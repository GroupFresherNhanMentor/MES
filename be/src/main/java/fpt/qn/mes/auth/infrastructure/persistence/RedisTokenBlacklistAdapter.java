package fpt.qn.mes.auth.infrastructure.persistence;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.application.port.out.TokenBlacklistPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisTokenBlacklistAdapter implements TokenBlacklistPort {

    static final String KEY_PREFIX = "jwt:blacklist:";

    StringRedisTemplate redisTemplate;

    @Override
    public void blacklistToken(String tokenId, Duration ttl) {
        if (tokenId == null || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        try {
            String key = KEY_PREFIX + tokenId;
            redisTemplate.opsForValue().set(key, "revoked", ttl);
        } catch (Exception ex) {
            log.warn("Failed to blacklist token in Redis: {}", ex.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        if (tokenId == null) {
            return false;
        }
        try {
            String key = KEY_PREFIX + tokenId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception ex) {
            log.warn("Failed to check token blacklist in Redis: {}", ex.getMessage());
            return false;
        }
    }
}
