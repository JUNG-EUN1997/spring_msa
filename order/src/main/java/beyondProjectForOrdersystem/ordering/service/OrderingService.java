package beyondProjectForOrdersystem.ordering.service;

import beyondProjectForOrdersystem.common.service.StockInventoryService;
import beyondProjectForOrdersystem.ordering.controller.SseController;
import beyondProjectForOrdersystem.ordering.domain.OrderDetail;
import beyondProjectForOrdersystem.ordering.domain.OrderStatus;
import beyondProjectForOrdersystem.ordering.domain.Ordering;
import beyondProjectForOrdersystem.ordering.dto.OrderListResDto;
import beyondProjectForOrdersystem.ordering.dto.OrderSaveReqDto;
import beyondProjectForOrdersystem.ordering.dto.OrderUpdateReqDto;
import beyondProjectForOrdersystem.ordering.repository.OrderDetailRepository;
import beyondProjectForOrdersystem.ordering.repository.OrderingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;

    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, StockDecreaseEventHandler stockDecreaseEventHandler, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
        this.sseController = sseController;
    }

    public Ordering orderCreate(List<OrderSaveReqDto> dtos){
//        필터 레이어에서 필터링된 토큰에 저장된 멤버 갖고오기 ⭐⭐⭐⭐⭐⭐
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // ⭐

        Ordering ordering = Ordering.builder()
                .memberEmail(memberEmail)
                .build();


//            MSA 분리작업을 진행해서, Product API에 요청을 해서, product 객체를 조회해야함.
        /*
        1. product name 을 얻기위해 product 서버에 get 요청
            >> 동기적으로 코드 해야함! 갔다 오기를 기다려야함!! :: 동기코드
            >> resttemplate, feighclient 라이브러리 또는 의존성 사용
        2. 주문시 product에 재고감소를 위해 product 서버에 patch 요청
            >> 비동기 처리
            >> 이 때, 상품서버에 바로 주문을 보내면 상품서버가 죽었을 때, 데이터가 유실될 수 있음
                >> 이 때, kafka 혹은 mq 등 써드파티 사용 예정
                >> 재고를 감소한다 라는 내용의 메시지(이벤트)을 넣을 예정
                    >> 이때, 재고 감소를 상품서버가 실패한다면, 상품서버가 다시 카프카에 넣고 그걸 주문서버에서 다시 불러와야한다.
                    >> transactional 이 안먹히기 때문에, try catch로 수동으로 작동할 예정이다.


        = 앞으로, 비동기 혹은 동기코드를 어떤식으로 구분할 것 인가? 이부분이 가장 중요!
        = 분리된 서버에서는 순환참조가 일어나지 않는다! 대신, 내부에서는 일어나니 받는 정보들은 dto를 사용해야한다.
        * */


        /*for (OrderSaveReqDto saveProduct : dtos) {

            if(product.getName().contains("sale")){
                int newQuantity = stockInventoryService.decreaseStock(saveProduct.getProductId()
                        ,saveProduct.getProductCount()).intValue();
                if(newQuantity < 0){
                    throw new IllegalArgumentException("재고 부족");
                }

                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), saveProduct.getProductCount()));

            }else{
                if(product.getStockQuantity() < saveProduct.getProductCount()){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
                product.updateStockQuantity("minus",saveProduct.getProductCount());
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .quantity(saveProduct.getProductCount())
                    .product(product)
                    .ordering(ordering)
                    .build();

            ordering.getOrderDetails().add(orderDetail);
        }*/

        Ordering savedOrdering = orderingRepository.save(ordering);

//        sse 알림용 코드 추가
        sseController.publicsMessage(savedOrdering.fromEntity(), "admin@test.com");

        return savedOrdering;
    }

    public Page<OrderListResDto> orderList(Pageable pageable){
        Page<Ordering> orderings =  orderingRepository.findAll(pageable);
        Page<OrderListResDto> orderListResDtos = orderings.map(a->a.fromEntity());

        return orderListResDtos;
    }

    public Page<OrderListResDto> myOrderList(Pageable pageable){
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Page<Ordering> orderings =  orderingRepository.findAllByMemberEmail(memberEmail, pageable);
        Page<OrderListResDto> orderListResDtos = orderings.map(a->a.fromEntity());
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("없는 주문입니다."));
        ordering.updateOrderStatus(OrderStatus.CANCELED);

        return ordering;
    }

}
