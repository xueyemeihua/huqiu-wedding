package com.huqiu.common.interceptor;

import com.huqiu.common.annotation.RequireRole;
import com.huqiu.common.context.LoginUser;
import com.huqiu.common.context.UserContext;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.result.ResultCode;
import com.huqiu.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        String token = req.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LoginUser user;
        try {
            user = jwtUtil.parse(token);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        UserContext.set(user);

        RequireRole rr = AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), RequireRole.class);
        if (rr == null) {
            rr = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), RequireRole.class);
        }
        if (rr != null && Arrays.stream(rr.value()).noneMatch(r -> r == user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        UserContext.clear();
    }
}