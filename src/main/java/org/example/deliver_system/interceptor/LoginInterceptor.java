package org.example.deliver_system.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.deliver_system.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            if (token != null) {
                Claims claims = jwtUtil.parseToken(token);
                // 将用户信息存入 Request 域，方便后续 Controller 使用
                // 注意 Claims 中存放的是 Integer 或 Long 取决于序列化，建议转为 String 再 parseLong
                Object userIdObj = claims.get("userId");
                if (userIdObj != null) {
                    request.setAttribute("userId", Long.parseLong(userIdObj.toString()));
                }
                request.setAttribute("username", claims.get("username"));
                request.setAttribute("role", claims.get("role"));
                return true;
            }
        } catch (Exception e) {
            // Token 解析失败
        }

        response.setStatus(401);
        // 设置响应头解决跨域时 OPTIONS 预检通过但 401 报错问题（虽然 CorsRegistry 已配，但拦截器直接返回可能跳过）
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.getWriter().write("Unauthorized");
        return false;
    }
}
