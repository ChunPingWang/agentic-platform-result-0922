package com.insurance.lifepremium.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Service
@Profile("!test")
public class RateCacheService {

    private static final Logger log = LoggerFactory.getLogger(RateCacheService.class);
    private static final String KEY_PREFIX = "rate:";

    private final StringRedisTemplate redisTemplate;
    private final long ttlSeconds;

    public RateCacheService(StringRedisTemplate redisTemplate,
                            @Value("${app.cache.rate.ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<BigDecimal> get(String productCode, String version, Integer age, String paymentPeriod) {
        String key = buildKey(productCode, version, age, paymentPeriod);
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Rate cache hit for key={}", key);
                return Optional.of(new BigDecimal(value));
            }
            log.debug("Rate cache miss for key={}", key);
        } catch (Exception e) {
            log.warn("Rate cache get failed for key={}: {}", key, e.getMessage());
        }
        return Optional.empty();
    }

    public void put(String productCode, String version, Integer age, String paymentPeriod, BigDecimal rate) {
        String key = buildKey(productCode, version, age, paymentPeriod);
        try {
            redisTemplate.opsForValue().set(key, rate.toPlainString(), Duration.ofSeconds(ttlSeconds));
            log.debug("Rate cached for key={}", key);
        } catch (Exception e) {
            log.warn("Rate cache put failed for key={}: {}", key, e.getMessage());
        }
    }

    private String buildKey(String productCode, String version, Integer age, String paymentPeriod) {
        return KEY_PREFIX + productCode + ":" + version + ":" + age + ":" + paymentPeriod;
    }
}