package com.region.lis.interceptor;

import com.region.lis.service.TokenService;
import com.region.lis.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



//@Component
//public class AuthInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private TokenUtil tokenUtil;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request,
//                             HttpServletResponse response,
//                             Object handler) throws Exception {
//
//        // 放行登录接口
//        if (request.getRequestURI().contains("/auth/login")) {
//            return true;
//        }
//
//        // 取 Token
//        String token = request.getHeader("Authorization");
//        if (token == null || token.isEmpty()) {
//            response.setStatus(401);
//            response.setCharacterEncoding("UTF-8");   // 加这一行
//            response.setContentType("application/json;charset=UTF-8");  // 加这一行
//            response.getWriter().write("{\"code\":401,\"message\":\"未携带Token\"}");
//            return false;
//        }
//
//        // 校验 Token
//        String orgId = tokenUtil.getOrgIdByToken(token);
//        if (orgId == null) {
//            response.setStatus(401);
//            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或过期\"}");
//            return false;
//        }
//
//        // 刷新 Token 有效期
//        tokenUtil.refreshToken(token);
//
//        // 把 orgId 存到 request，后续业务用
//        request.setAttribute("orgId", orgId);
//
//        return true;
//    }
//}



@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录接口
//        String uri = request.getRequestURI();
//        if (uri.contains("/auth/login")) {
//            return true;
//        }
        log.info("🔍 拦截器拦截路径：{}", request.getRequestURI());

        // 取 Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setCharacterEncoding("UTF-8");   // 加这一行
            response.setContentType("application/json;charset=UTF-8");  // 加这一行
            return writeError(response, 401, "未携带Token666");
        }

        // 校验 Token
        if (!tokenService.validate(token)) {
            return writeError(response, 401, "Token无效或已过期");
        }

        // 刷新有效期
        tokenService.refresh(token);
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