package beyondProjectForOrdersystem.ordering.dto;

import beyondProjectForOrdersystem.ordering.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    @Builder.Default
    private List<OrderDetailDto> orderDetailDtos = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderDetailDto{
        private Long id;
        private String productName;
        private Integer count;
    }
}
