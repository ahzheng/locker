package com.popdue.locker;

import com.popdue.locker.core.RedisLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(LockImportSeletor.class)
@EnableConfigurationProperties({LockProperties.class})
public class LockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisLock redisLock() {
        return new RedisLock();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisLock redisLock(LockProperties lockProperties) {
        RedisLock redisLock = new RedisLock();
        return redisLock;
    }
}
