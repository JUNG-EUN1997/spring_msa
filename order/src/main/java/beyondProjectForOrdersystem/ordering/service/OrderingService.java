package beyondProjectForOrdersystem.ordering.service;

import beyondProjectForOrdersystem.common.service.StockInventoryService;
import beyondProjectForOrdersystem.member.domain.Member;
import beyondProjectForOrdersystem.member.dto.StockDecreaseEvent;
import beyondProjectForOrdersystem.member.repository.MemberRepository;
import beyondProjectForOrdersystem.ordering.controller.SseController;
import beyondProjectForOrdersystem.ordering.domain.OrderDetail;
import beyondProjectForOrdersystem.ordering.domain.OrderStatus;
import beyondProjectForOrdersystem.ordering.domain.Ordering;
import beyondProjectForOrdersystem.ordering.dto.OrderListResDto;
import beyondProjectForOrdersystem.ordering.dto.OrderSaveReqDto;
import beyondProjectForOrdersystem.ordering.dto.OrderUpdateReqDto;
import beyondProjectForOrdersystem.ordering.repository.OrderDetailRepository;
import beyondProjectForOrdersystem.ordering.repository.OrderingRepository;
import beyondProjectForOrdersystem.product.domain.Product;
import beyondProjectForOrdersystem.product.repository.ProductRepository;
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
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;

    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, MemberRepository memberRepository, ProductRepository productRepository, StockInventoryService stockInventoryService, StockDecreaseEventHandler stockDecreaseEventHandler, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.stockInventoryService = stockInventoryService;
        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
        this.sseController = sseController;
    }

/*
     동시성 이슈 처리
     1. synchronized 설정 시
         : 설정한다 하더라고, 재고 감소가 DB에 반영되는 시점은 트랜잭션이 커밋되고 종료되는 시점.
             : 해당 메소드에 들어오지 못한다는 것 이지, DB는 서드파티 부분이라, 실질적으로 재고가 달라지는 커밋되고 종료되는 시점과 다르다.
         : 따라서, synchronized를 걸어도 해결되지 못하는 경우도 있다.
     2.
*/
    public Ordering orderCreate(List<OrderSaveReqDto> dtos){
//        ⭐방법1.⭐쉬운방식
////        Ordering생성 : member_id, status
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("없음"));
//        Ordering ordering = orderingRepository.save(dto.toEntity(member));
//
////        OrderDetail생성 : order_id, product_id, quantity
//        for(OrderSaveReqDto.OrderDetailDto orderDto : dto.getOrderDetailDtoList()){
//            Product product = productRepository.findById(orderDto.getProductId()).orElse(null);
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail =  OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }
//
//        return ordering;


//        ⭐방법2.⭐ JPA에 적합한 방식 :: [OrderDetailRepository]가 필요없는 경우
//        Member member = memberRepository.findById(dto.getMemberId())
//                .orElseThrow(()-> new EntityNotFoundException("없는 유저입니다."));


//        필터 레이어에서 필터링된 토큰에 저장된 멤버 갖고오기 ⭐⭐⭐⭐⭐⭐
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // ⭐
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(()-> new EntityNotFoundException("없는 유저입니다."));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderSaveReqDto saveProduct : dtos) {
            Product product = productRepository.findById(saveProduct.getProductId())
                    .orElseThrow(()-> new EntityNotFoundException("없는 상품 입니다."));

//            동시성 이슈로 인해, 재고감소 영역 갱신이상 발생
//              따라서, redis를 통한 재고관리 및 재고잔량 확인 :: 멀티스레드 발생 원천차단
            if(product.getName().contains("sale")){ // 현재 sale 문구 유무를 통해 redis 관리 여부 체크
//                redis를 통한 재고관리 및 재고잔량 확인
                int newQuantity = stockInventoryService.decreaseStock(saveProduct.getProductId()
                        ,saveProduct.getProductCount()).intValue();
                if(newQuantity < 0){
                    throw new IllegalArgumentException("재고 부족");
                }

//                RDB에 재고 업데이트 필요 ⭐⭐
//                  rabbitmq를 통해 비동기적으로 이벤트 처리
                /*
                * 방법 1)
                * 스케쥴러를 통해 일정 시간에 만 동기화 시키기 : ex) 1분에 1번
                * 방법 2)
                * 이벤트 기반의 아키텍처 구상하기 : event driven
                *   - 또 다른 서드파티 구상
                *   - MQ(Queing 서비스)를 새로 제작하여 거기에 구상함
                *       - ex) Queing 서비스의 예시 : rabbitmq, 카프카 라는 서비스가 있음
                *   - MQ에 요청을 넣음 : publish
                *   - MQ에서 요청을 갖고옴 : listen
                *       - 요청을 넣고 갖고오는 것은 같은 스프링 내부에서 진행이 된다.
                *   - 큐잉 서비스에 넣으면, 데이터가 유실되지 않는다
                * (예시)
                * - 주문 사이트에서, 구매하려고 할 때는 재고가 있었는데 몇 초차로 없는 경우를 볼 수 있다.
                *
                * */
                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), saveProduct.getProductCount()));

            }else{
                if(product.getStockQuantity() < saveProduct.getProductCount()){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
                product.updateStockQuantity("minus",saveProduct.getProductCount());
                //변경감지(dirty checking) 으로 save 불필요
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .quantity(saveProduct.getProductCount())
                    .product(product)
                    .ordering(ordering)
                    .build();

            ordering.getOrderDetails().add(orderDetail); // ⭐⭐ orderDetailRepository.save 대신 add
//            JPA의 영속성 컨텍스트를 통해 생성이 가능한 방법이다.
        }

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
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(()-> new EntityNotFoundException("없는 유저입니다."));

        Page<Ordering> orderings =  orderingRepository.findAllByMember(member, pageable);
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
