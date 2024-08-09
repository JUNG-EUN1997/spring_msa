package beyondProjectForOrdersystem.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockInventoryService {
    @Qualifier("3")
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public StockInventoryService(@Qualifier("3") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

//    상품 등록 시 increaseStock 호출
    public Long increaseStock(Long itemId, Integer quantity){
//        🍀 추가 로직 개발 필요 🍀
//              redis가 음수까지 내려갈 경우 추후 재고 update 상황에서 increase 값이 정확하지 않을 수 잇으니
//              음수이면 0으로 setting 로직 필요

//        아래 메서드의 리턴 값은 잔량값을 리턴
        return redisTemplate.opsForValue().increment(String.valueOf(itemId), quantity);
//        increment : 숫자(quantity)값을 매개로 키값의 value를 더해 줌
    }

//    주문 등록 시 decreaseStock 호출
    public Long decreaseStock(Long itemId, Integer quantity){
        Object remains = redisTemplate.opsForValue().get(String.valueOf(itemId));
        // redis는 전부 문자열로 되어있어서 string으로 넣고 빼야함

        int intRemains = Integer.parseInt(remains.toString());
        if (intRemains < quantity){
            return -1L;
        }else {

//          남아있는 잔량을 리턴
            return redisTemplate.opsForValue().decrement(String.valueOf(itemId), quantity);
//        decrement : 숫자(quantity)값을 매개로 키값의 value를 빼줌
        }

    }

}
