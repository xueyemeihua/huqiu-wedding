package com.huqiu.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 业务单号生成：前缀 + yyyyMMdd + 4 位流水号，Redis 保证每日自增且不重复
 */
@Component
@RequiredArgsConstructor
public class OrderNoGenerator {

    private final StringRedisTemplate redisTemplate;

    /** 销售单前缀 SO / 改衣单 TO */
    public String next(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "order:no:" + prefix + ":" + date;
        Long seq = redisTemplate.opsForValue().increment(key);
        if (seq != null && seq == 1) {
            redisTemplate.expire(key, Duration.ofDays(2));
        }
        return prefix + date + String.format("%04d", seq);
    }
}