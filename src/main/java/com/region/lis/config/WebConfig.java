package com.region.lis.config;

import com.region.lis.interceptor.AuditInterceptor;
import com.region.lis.interceptor.AuthInterceptor;
import com.region.lis.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Autowired
//    private AuthInterceptor authInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(authInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/auth/login");
//    }
//
//
//}

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private AuditInterceptor auditInterceptor;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        // 鉴权拦截器
//        registry.addInterceptor(authInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns(
//                        "/auth/login",            // 登录放行
//                        "/auth/register",         // 注册放行
//                        "/auth/reset-password"    // 重置密码放行
//                ); // 1. 鉴权拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        // /auth/reset-password 为管理员接口，必须鉴权，不再放行
                        "/report/**",       // ← 放行 report 目录下的图片

                        "/error"
                );

        // 2. 权限拦截器
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/report/**",       // ← 放行 report 目录下的图片

//                        "/auth/reset-password",
                        "/error"
                );

        // 3. 审计拦截器
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/**");
    }
}