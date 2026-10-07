package com.region.lis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TokenUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long EXPIRE_MINUTES = 30;

    /**
     * 生成 Token，存 Redis
     */
    public String createToken(String orgId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("token:" + token, orgId, EXPIRE_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 校验 Token
     */
    public String getOrgIdByToken(String token) {
        return redisTemplate.opsForValue().get("token:" + token);
    }

    /**
     * 刷新 Token
     */
    public void refreshToken(String token) {
        String orgId = getOrgIdByToken(token);
        if (orgId != null) {
            redisTemplate.expire("token:" + token, EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }
}