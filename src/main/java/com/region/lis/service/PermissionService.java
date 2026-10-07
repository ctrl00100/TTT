package com.region.lis.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.region.lis.entity.SysPermission;
import com.region.lis.entity.SysRolePermission;
import com.region.lis.entity.SysUserRole;
import com.region.lis.mapper.SysPermissionMapper;
import com.region.lis.mapper.SysRolePermissionMapper;
import com.region.lis.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PermissionService {

    @Autowired private SysUserRoleMapper sysUserRoleMapper;
    @Autowired private SysRolePermissionMapper sysRolePermissionMapper;
    @Autowired private SysPermissionMapper sysPermissionMapper;

    /**
     * 根据用户ID，查所有权限编码
     */
    public List<String> getPermCodesByUserId(Long userId) {
        // 1. 查用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());

        // 2. 查角色权限
        List<SysRolePermission> rolePerms = sysRolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
        );
        if (rolePerms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> permIds = rolePerms.stream()
            .map(SysRolePermission::getPermId)
            .collect(Collectors.toList());

        // 3. 查权限编码
        List<SysPermission> perms = sysPermissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, permIds)
        );

        return perms.stream()
            .map(SysPermission::getPermCode)
            .collect(Collectors.toList());
    }
}