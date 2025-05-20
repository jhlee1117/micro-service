package com.microservices.api_gateway;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    // application.yml에 정의된 Lettuce pool 설정을 가져오기 위한 값들
    @Value("${spring.data.redis.lettuce.pool.max-active:8}") // 기본값 8
    private int maxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}") // 기본값 8
    private int maxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:0}") // 기본값 0
    private int minIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:-1ms}") // 기본값 -1 (무한 대기)
    private long maxWaitMillis;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost,
                redisPort);
        // 비밀번호가 있다면 설정:
        // redisStandaloneConfiguration.setPassword(RedisPassword.of("your-password"));

        // Lettuce 풀 설정
        GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        // poolConfig.setTestOnBorrow(true); // 풀에서 커넥션을 가져올 때 유효성 검사 (성능에 영향 줄 수 있음)
        // poolConfig.setTestOnReturn(true); // 풀에 커넥션을 반환할 때 유효성 검사 (성능에 영향 줄 수 있음)

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                // .commandTimeout(Duration.ofMillis(timeout)) // application.yml의
                // spring.data.redis.timeout 사용 가능
                .build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // application.yml의 값으로 생성된 connectionFactory 사용
        template.setConnectionFactory(connectionFactory);

        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

}
