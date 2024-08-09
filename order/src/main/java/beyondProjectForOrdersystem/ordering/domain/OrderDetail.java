package beyondProjectForOrdersystem.ordering.domain;

import beyondProjectForOrdersystem.common.domain.BaseTimeEntity;
import beyondProjectForOrdersystem.ordering.dto.OrderListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class OrderDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;


    private Long productId;

    public OrderListResDto.OrderDetailDto fromEntity(){
        return OrderListResDto.OrderDetailDto
                .builder()
                .id(this.id)
//                .productName(this.product.getName())
                .count(this.quantity)
                .build();
    }
}
