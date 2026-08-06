package org.mini_lab.file_upload_service.security.rate_limiter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration(proxyBeanMethods = false)
public class RedisScriptConfiguration {

    @Bean
    DefaultRedisScript<Long> incrementWithExpirationScript() {
        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>();

        script.setLocation(
                new ClassPathResource(
                        "redis/increment-with-expiration.lua"
                )
        );

        script.setResultType(Long.class);

        return script;
    }
}