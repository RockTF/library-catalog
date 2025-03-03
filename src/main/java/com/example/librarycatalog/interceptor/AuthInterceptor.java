package com.example.librarycatalog.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String VALID_TOKEN = "secret-token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"error\":\"Unauthorized\",\"code\":401,\"message\":\"Missing or invalid token.\"}");
            return false;
        }
        String token = authHeader.substring(7);
        if (!VALID_TOKEN.equals(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"error\":\"Unauthorized\",\"code\":401,\"message\":\"Invalid token.\"}");
            return false;
        }
        return true;
    }
}
