package beyondProjectForOrdersystem.common.configs;

import beyondProjectForOrdersystem.ordering.controller.SseController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig { // DB는 총 16개 존재
//    application.yml의 spring.redis.host의 정보를 소스코드의 변수로 가져오는 것

    @Value("${spring.redis.host}")
    public String host;

    @Value("${spring.redis.port}")
    public int port;

    @Bean
    @Qualifier("2")
//    RedisConnectionFactory 는 Redis 서버와의 연결을 설정 하는 역할
//    LettuceConnectionFactory 는 RedisConnectionFactory 의 구현체로서 실질적인 역할 수행
    public RedisConnectionFactory redisConnectionFactory() {
//        return new LettuceConnectionFactory("localhost", 6379);
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1); // 1번 DB 사용
//        configuration.setPassword("1234"); // 비밀번호
        return new LettuceConnectionFactory(configuration);
    }
    //        redisTemplate은 redis와 상호작용 할 때 redis key, value 의 형식을 정의
    @Bean
    @Qualifier("2")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("2") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
//    redisTemplate.opsForValue().set(key, value)
//    redisTemplate.opsForValue().get(key)
//    redisTemplate.opsForValue().increment 또는 decrement

    
    
    
//    product 재고감소를 위한 redis DB 세팅
    @Bean
    @Qualifier("3")
    public RedisConnectionFactory redisStockFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(2); // 2번 DB 사용
        return new LettuceConnectionFactory(configuration);
    }
    
    @Bean
    @Qualifier("3")
    public RedisTemplate<String, Object> redisStockTemplate(@Qualifier("3") RedisConnectionFactory redisStockFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(redisStockFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("4")
    public RedisConnectionFactory redisSseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(3); // 3번 DB 사용
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("4")
    public RedisTemplate<String, Object> redisSseTemplate(@Qualifier("4") RedisConnectionFactory redisSseFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());

//        보내는 객체 내부에 또다른 객체가 존재하여 직렬화 이슈 발생
//          따라서 아래와 같이 serializer 커스텀 진행
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        serializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(serializer);

        redisTemplate.setConnectionFactory(redisSseFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("4")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("4")
                                                                           RedisConnectionFactory redisSseFactory){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisSseFactory());
        return container;
    }


//    사용 XXXXXXX!
//    redis에 메시지 발행 시, MessageListenerAdapter를 통해 listen 하게 되고,
//          아래 코드를 통해 특정 메서드를 실행하도록 설정
//    @Bean
//    public MessageListenerAdapter listenerAdapter(SseController sseController){
//        return new MessageListenerAdapter(sseController, "onMessage");
//    }



}