package com.huqiu.common.util;

import com.huqiu.common.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-seconds}")
    private long expireSeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(LoginUser user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("merchantId", user.getMerchantId())
                .claim("username", user.getUsername())
                .claim("realName", user.getRealName())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    public LoginUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        LoginUser u = new LoginUser();
        u.setUserId(Long.valueOf(claims.getSubject()));
        u.setMerchantId(claims.get("merchantId", Long.class));
        u.setUsername(claims.get("username", String.class));
        u.setRealName(claims.get("realName", String.class));
        u.setRole(claims.get("role", Integer.class));
        return u;
    }
}