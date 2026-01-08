package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
@Slf4j
public class JwtTokenUserlnterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        String token=request.getHeader(jwtProperties.getUserTokenName());
        // 2. 【关键修复】判断 token 是否为空
        if (token == null || token.isEmpty()) {
            log.error("用户端未传递令牌");
            response.setStatus(401); // 未授权
            return false;
        }
        try {

            String secretKey=jwtProperties.getUserSecretKey();
            Claims claims = jwtUtil.parseJWT(secretKey, token);
            Object o = claims.get(JwtClaimsConstant.USER_ID);//jwt在网络传输会先序列化成base64，然后变成JSON他并没有保留之前是什么类型统一用JSON
            Long userid=Long.valueOf( o.toString());
            BaseContext.setCurrentId(userid);
            return true;
        } catch (NumberFormatException e) {
           response.setStatus(403);
            return false;
        }

    }
}
