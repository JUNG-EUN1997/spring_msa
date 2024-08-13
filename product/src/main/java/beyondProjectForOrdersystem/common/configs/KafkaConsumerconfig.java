//package beyondProjectForOrdersystem.common.configs;
//
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
//import org.springframework.util.backoff.FixedBackOff;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@EnableKafka
//@Configuration
//public class KafkaConsumerconfig {
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Value("${spring.kafka.consumer.group-id}")
//    private String groupId;
//
//    @Value("${spring.kafka.consumer.auto-offset-reset}")
//    private String autoOffSet;
//
//    @Bean
////    원래 프로듀서가, <String, Object> 형식으로 보내줘서, Object로 해줘야하는데,
////          그렇게 된다면 어떤 클래스 형식으로 받을 것 인지 선언을 따로 해줘야하고 (ProductUpdateStockDto)
////          너무 한정적인 사용 방식이기 때문에 String으로 받고 ObjectMapper로 다시 파싱해주는 방식 사용
//    public ConsumerFactory<String, String> consumerFactory(){
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffSet);
//
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(){
//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//
////        setErrorHandler : error 났을 때 어떻게 할 것 인가?
////              에러가 났을 경우, 3초에 1번씩 재요청
//        factory.setErrorHandler(new SeekToCurrentErrorHandler(new FixedBackOff(1000L, 3)));
//
//        return factory;
//    }
//}
