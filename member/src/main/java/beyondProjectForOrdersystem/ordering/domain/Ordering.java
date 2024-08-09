package beyondProjectForOrdersystem.ordering.domain;

import beyondProjectForOrdersystem.common.domain.BaseTimeEntity;
import beyondProjectForOrdersystem.member.domain.Member;
import beyondProjectForOrdersystem.ordering.dto.OrderListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ordering extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Builder.Default // 초기값을 설정해주려면 이렇게 해줘야함
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    @Builder.Default
//    Builder패턴에서도 ArrayList로 초기화 되도록 하는 설정 >> 빌더패턴을 쓰게되면 선언한 것을 갖고오기 때문이다!
    private List<OrderDetail> orderDetails = new ArrayList<>();
//    ordering을 통해서 orderDetail을 쉽게 갖고오려고 선언
//    + ordering을 통해 orderDetail을 생성 및 수정하려고 선언
//          ++ 해당 기능은 [cascade = CascadeType.PERSIST] 가 없으면 사용 불가



    public OrderListResDto fromEntity(){
        OrderListResDto orderListResDto = OrderListResDto.builder()
                .id(this.id)
                .memberEmail(this.member.getEmail())
                .orderStatus(this.orderStatus)
                .build();

        for (OrderDetail detail : this.getOrderDetails()) {

//            OrderListResDto.OrderDetailDto orderDetailDto = OrderListResDto.OrderDetailDto.builder()
//                    .id(detail.getId())
//                    .productName(detail.getProduct().getName())
//                    .count(detail.getQuantity())
//                    .build();
//            orderListResDto.getOrderDetailDtos().add(orderDetailDto);

            orderListResDto.getOrderDetailDtos().add(detail.fromEntity());
        }

        return orderListResDto;
    }

    public void updateOrderStatus(OrderStatus status){
        this.orderStatus = status;
    }

}
