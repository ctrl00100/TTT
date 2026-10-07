package com.region.lis.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.region.lis.entity.SysPermission;
import com.region.lis.mapper.SysPermissionMapper;
import com.region.lis.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@Slf4j
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired private StringRedisTemplate stringRedisTemplate;
    @Autowired private SysPermissionMapper sysPermissionMapper;
    @Autowired private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. 查这个接口需要什么权限
        SysPermission perm = sysPermissionMapper.selectOne(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getApiPath, uri)
                .eq(SysPermission::getMethod, method)
        );

        // 接口没配权限，直接放行
        if (perm == null) {
            return true;
        }

        // 2. 拿当前用户权限
        String token = request.getHeader("Authorization");
        String orgCode = tokenService.getOrgCode(token);
        if (orgCode == null) {
            return writeError(response, 401, "Token无效");
        }

        String perms = stringRedisTemplate.opsForValue().get("user:perms:" + orgCode);
        log.info("🔒 权限校验：接口={}, 需要={}, 用户权限={}", uri, perm.getPermCode(), perms);

        // 3. 校验
        if (perms == null || !perms.contains(perm.getPermCode())) {
            return writeError(response, 403, "无权限访问：" + perm.getPermName());
        }

        return true;
    }

    private boolean writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
        return false;
    }
}