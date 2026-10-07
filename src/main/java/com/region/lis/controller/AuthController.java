package com.region.lis.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.region.lis.common.Result;
import com.region.lis.entity.OrgAccount;
import com.region.lis.mapper.OrgAccountMapper;
import com.region.lis.service.PermissionService;
import com.region.lis.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private OrgAccountMapper orgAccountMapper;
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestParam String orgCode,
                                             @RequestParam String password) {
        OrgAccount account = orgAccountMapper.selectOne(
                new LambdaQueryWrapper<OrgAccount>()
                        .eq(OrgAccount::getOrgCode, orgCode)
                        .eq(OrgAccount::getStatus, 1)
        );

        if (account == null) {
            return Result.fail(401, "机构不存在或已禁用");
        }

        // 连续失败5次锁定
        if (account.getFailCount() != null && account.getFailCount() >= 5) {
            return Result.fail(403, "账号已锁定，请联系管理员");
        }

        // 校验密码
        if (!BCrypt.checkpw(password, account.getPassword())) {
            account.setFailCount((account.getFailCount() == null ? 0 : account.getFailCount()) + 1);
            orgAccountMapper.updateById(account);
            return Result.fail(401, "密码错误，连续失败" + account.getFailCount() + "次");
        }

        // 登录成功，清空失败次数，更新最后登录时间
        account.setFailCount(0);
        account.setLastLoginTime(LocalDateTime.now());
        orgAccountMapper.updateById(account);

        // 查用户权限
        List<String> permCodes = permissionService.getPermCodesByUserId(account.getId());

        // 存 Redis（30分钟过期）
        String permKey = "user:perms:" + orgCode;
        stringRedisTemplate.opsForValue().set(
                permKey,
                String.join(",", permCodes),
                30, TimeUnit.MINUTES
        );

        log.info("🔑 用户 {} 权限：{}", orgCode, permCodes);

        String token = tokenService.generate(orgCode);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("orgName", account.getOrgName());
        data.put("expireMinutes", "30");
        data.put("perms", String.join(",", permCodes));

        return Result.success(data);
    }


    @PostMapping("/register")
    public Result<String> register(@RequestParam String orgCode,
                                   @RequestParam String orgName,
                                   @RequestParam String password) {
        // 1. 机构编码是否已存在
        Long count = orgAccountMapper.selectCount(
                new LambdaQueryWrapper<OrgAccount>().eq(OrgAccount::getOrgCode, orgCode)
        );
        if (count > 0) {
            return Result.fail(400, "机构编码已存在");
        }

        // 2. 密码强度
        if (password == null || password.length() < 6) {
            return Result.fail(400, "密码至少6位");
        }

        // 3. 入库
        OrgAccount account = new OrgAccount();
        account.setOrgCode(orgCode);
        account.setOrgName(orgName);
        account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        account.setStatus(1);
        account.setFailCount(0);
        account.setPasswordUpdateTime(LocalDateTime.now());
        orgAccountMapper.insert(account);

        return Result.success("注册成功");
    }

    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestHeader("Authorization") String token,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        String orgCode = tokenService.getOrgCode(token);
        if (orgCode == null) {
            return Result.fail(401, "Token无效");
        }

        OrgAccount account = orgAccountMapper.selectOne(
                new LambdaQueryWrapper<OrgAccount>().eq(OrgAccount::getOrgCode, orgCode)
        );
        if (account == null) {
            return Result.fail(404, "机构不存在");
        }

        if (!BCrypt.checkpw(oldPassword, account.getPassword())) {
            return Result.fail(401, "旧密码错误");
        }

        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail(400, "新密码至少6位");
        }

        if (oldPassword.equals(newPassword)) {
            return Result.fail(400, "新密码不能与旧密码相同");
        }

        account.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        account.setPasswordUpdateTime(LocalDateTime.now());
        orgAccountMapper.updateById(account);

        // 强制重新登录
        tokenService.remove(token);

        return Result.success("密码修改成功，请重新登录");
    }
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestParam String orgCode,
                                        @RequestParam String newPassword
//            ,
//                                        @RequestParam String adminKey
    ) {
//        if (!"ADMIN_SECRET_KEY".equals(adminKey)) {
//            return Result.fail(403, "无权限");
//        }

        OrgAccount account = orgAccountMapper.selectOne(
                new LambdaQueryWrapper<OrgAccount>().eq(OrgAccount::getOrgCode, orgCode)
        );
        if (account == null) {
            return Result.fail(404, "机构不存在");
        }

        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail(400, "新密码至少6位");
        }

        account.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        account.setPasswordUpdateTime(LocalDateTime.now());
        account.setFailCount(0);
        orgAccountMapper.updateById(account);

        return Result.success("密码重置成功");
    }
    @GetMapping("/current")
    public Result<Map<String, Object>> current(@RequestHeader("Authorization") String token) {
        String orgCode = tokenService.getOrgCode(token);
        if (orgCode == null) {
            return Result.fail(401, "Token无效");
        }

        OrgAccount account = orgAccountMapper.selectOne(
                new LambdaQueryWrapper<OrgAccount>().eq(OrgAccount::getOrgCode, orgCode)
        );

        Map<String, Object> data = new HashMap<>();
        data.put("orgCode", account.getOrgCode());
        data.put("orgName", account.getOrgName());
        data.put("lastLoginTime", account.getLastLoginTime());
        data.put("passwordUpdateTime", account.getPasswordUpdateTime());
        return Result.success(data);
    }
}