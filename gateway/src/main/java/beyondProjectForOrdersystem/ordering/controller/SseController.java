package beyondProjectForOrdersystem.ordering.controller;

import beyondProjectForOrdersystem.ordering.dto.OrderListResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.security.auth.message.AuthException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SseController implements MessageListener {
//    SseEmitter는 연결된 사용자 정보를 의미
//        String은 email을 의미
//    ConcurrentHashMap은 Thread-safe한 map이다.
//          멀티스레드 상황에서 문제가 없는 맵이다. : 동시성 이슈 발생 X

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private Set<String> subscribeList = ConcurrentHashMap.newKeySet();

    @Qualifier("4")
    private final RedisTemplate<String, Object> redisSseTemplate;

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public SseController(RedisMessageListenerContainer redisMessageListenerContainer, @Qualifier("4")RedisTemplate<String, Object> redisSseTemplate) {
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.redisSseTemplate = redisSseTemplate;
    }

//    email에 해당되는 메시지를 listen하는 listener를 추가한 것.
    public void subscribeChannel(String email){
//        순서 상, subscribe(해당 이메일을 listen)하면,
//              createListenerAdapter 로 인해 아래의 [onMessage] 메서드를 실행함
        if(!subscribeList.contains(email)){ //이미 구독한 email일 경우에는 더 이상 구독하지 않는 분기처리
            MessageListenerAdapter listenerAdapter = createListenerAdapter(this);
            redisMessageListenerContainer.addMessageListener(listenerAdapter, new PatternTopic(email));
            subscribeList.add(email);
        }
    }

    private MessageListenerAdapter createListenerAdapter(SseController sseController){
        return new MessageListenerAdapter(sseController,"onMessage");
    }

    //    유저와 서버가 연결을 시작하는 곳
    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
        SseEmitter emitter = new SseEmitter(14400*60*1000L); // 24시간의 유효시간
//        사용자와 관련된 정보가 들어있는 SseEmitter

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitters.put(email,emitter);

        emitter.onCompletion(() -> emitters.remove(email)); // 완료되면 map에서 삭제
        emitter.onTimeout(() -> emitters.remove(email)); // timeout 되어도 map에서 삭제

        try {
            emitter.send(SseEmitter.event().name("connect").data("connect success!"));
        }catch (IOException e){
            e.printStackTrace();
        }

//        ⭐⭐⭐⭐⭐ : 해당 이메일을 subscribe 하는 코드 실행
//          처음, front에서 subscribe 요청을 보낼 때, redis 서버에서도 subscribe를 진행하겠다는 의미
        subscribeChannel(email);

        return emitter;
    }

    public void publicsMessage(OrderListResDto dto, String email){
        SseEmitter emitter = emitters.get(email);
//        if(emitter != null){
//            try {
//                emitter.send(SseEmitter.event().name("ordered").data(dto));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }else{ //redis에 던지기
//            convertAndSend : 직렬화해서 보내겠다
            redisSseTemplate.convertAndSend(email, dto);
//        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
//        message 내용 parsing
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OrderListResDto dto = objectMapper.readValue(message.getBody(), OrderListResDto.class);

            String email = new String(pattern, StandardCharsets.UTF_8);
            SseEmitter emitter = emitters.get(email);
            if(emitter != null){
                emitter.send(SseEmitter.event().name("ordered").data(dto));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
