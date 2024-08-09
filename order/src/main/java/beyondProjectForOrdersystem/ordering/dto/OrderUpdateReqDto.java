package beyondProjectForOrdersystem.ordering.dto;

import beyondProjectForOrdersystem.ordering.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateReqDto {
    private Long id;
    private OrderStatus orderStatus;
}
