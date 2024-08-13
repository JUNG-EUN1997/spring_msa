//package beyondProjectForOrdersystem.ordering.service;
//
//import beyondProjectForOrdersystem.common.configs.RabbitMqConfig;
//import beyondProjectForOrdersystem.ordering.dto.StockDecreaseEvent;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//public class StockDecreaseEventHandler {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//
////    orderService 레벨에서 발행
//    public void publish(StockDecreaseEvent event){
////        rabbitTemplate.convertAndSend(큐이름, json(객체));
//        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
//    }
//
//
////    @Transactional // Component 에서도 Transactional 처리 가능
////    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
////    public void listen(){
//////        RDB update 처리를 진행해주는 곳
////
////
////    }
//}
