package com.region.lis.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


public class RequestUtil {

    /**
     * 取当前请求的 IP
     * 定时任务/异步线程返回 "SYSTEM"
     */
    public static String getIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "SYSTEM";
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    /**
     * 取当前请求的 Token
     */
    public static String getToken() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest().getHeader("Authorization");
    }

    /**
     * 取当前请求的机构编码（从 Token 解析，需配合 TokenService）
     */
    public static String getOrgCode() {
        String token = getToken();
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 注意：这里不能直接注入 TokenService，静态方法里没法用
        // 如果需要 orgCode，建议在 Service 层注入 TokenService 获取
        return null;
    }
}