package com.region.lis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_PREFIX = "lis:token:";
    private static final long EXPIRE_MINUTES = 30;

    /**
     * 生成 Token
     */
    public String generate(String orgCode) {

//        // 1. 先删掉该机构旧的 Token
//        String oldTokenKey = "lis:token:org:" + orgCode;
//        String oldToken = stringRedisTemplate.opsForValue().get(oldTokenKey);
//        if (oldToken != null) {
//            stringRedisTemplate.delete("lis:token:" + oldToken);
//        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, orgCode, EXPIRE_MINUTES, TimeUnit.MINUTES);
//        stringRedisTemplate.opsForValue().set("lis:token:" + token, orgCode, 30, TimeUnit.MINUTES);

        log.info("🔑 生成Token：机构={}, token={}", orgCode, "token");
        return token;
    }

    /**
     * 校验 Token
     */
    public boolean validate(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String key = TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 刷新 Token 有效期
     */
    public void refresh(String token) {
        String key = TOKEN_PREFIX + token;
        stringRedisTemplate.expire(key, EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 删除 Token
     */
    public void remove(String token) {
        stringRedisTemplate.delete(TOKEN_PREFIX + token);
    }
    public String getOrgCode(String token) {
        String key = TOKEN_PREFIX + token;
        return stringRedisTemplate.opsForValue().get(key);
    }

}