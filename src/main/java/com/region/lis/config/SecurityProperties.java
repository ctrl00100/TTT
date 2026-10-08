package com.region.lis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全相关配置。所有取值均来自配置文件/环境变量，禁止在代码中写死。
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 管理员机构编码。仅这些机构可调用 /auth/reset-password 重置他机构密码。
     * 留空表示拒绝所有调用（fail-close）。
     */
    private List<String> adminOrgCodes = new ArrayList<>();

    /**
     * 必须配置权限记录的接口清单。命中该清单但 sys_permission 中查不到对应记录的接口，
     * 一律拒绝访问（fail-close），避免出现"漏配权限等于对外开放"。
     */
    private List<String> failClosePaths = new ArrayList<>();

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 判断机构编码是否属于管理员机构
     */
    public boolean isAdminOrg(String orgCode) {
        if (orgCode == null || orgCode.isBlank()) {
            return false;
        }
        String caller = orgCode.trim();
        return adminOrgCodes.stream()
                .map(String::trim)
                .anyMatch(code -> !code.isEmpty() && code.equalsIgnoreCase(caller));
    }

    /**
     * 判断接口是否属于强制权限配置清单
     */
    public boolean isFailClosePath(String uri) {
        return failClosePaths.stream()
                .filter(path -> path != null && !path.isBlank())
                .anyMatch(path -> pathMatcher.match(path.trim(), uri));
    }
}
