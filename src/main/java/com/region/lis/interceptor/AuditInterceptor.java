package com.region.lis.interceptor;

import com.region.lis.entity.AuditLog;
import com.region.lis.mapper.AuditLogMapper;
import com.region.lis.service.TokenService;
import com.region.lis.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



@Component
@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private TokenService tokenService;

    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        START_TIME.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Long start = START_TIME.get();
            long cost = start == null ? 0 : System.currentTimeMillis() - start;

            AuditLog log = new AuditLog();
            log.setApiPath(request.getRequestURI());
            log.setMethod(request.getMethod());
            log.setIp(getIp(request));
            log.setResponseCode(response.getStatus());
            log.setCostTime(cost);

            // 取 Token 对应的机构
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                String orgCode = tokenService.getOrgCode(token);
                log.setOrgCode(orgCode);
            }

            auditLogMapper.insert(log);
        } catch (Exception e) {
            log.error("审计日志记录失败：{}", e.getMessage());
        } finally {
            START_TIME.remove();
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

//        log.setIp(RequestUtil.getIp());
        return ip;
    }
}