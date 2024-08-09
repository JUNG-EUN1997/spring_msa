package beyondProjectForOrdersystem.ordering.dto;

import beyondProjectForOrdersystem.member.domain.Member;
import beyondProjectForOrdersystem.ordering.domain.OrderDetail;
import beyondProjectForOrdersystem.ordering.domain.OrderStatus;
import beyondProjectForOrdersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSaveReqDto {
    private Long productId;
    private Integer productCount;

//    private Long memberId;
//    private List<OrderSaveReqDto.OrderDetailDto> orderDetailDtoList;

//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Builder
//    public static class OrderDetailDto{
//        private Long productId;
//        private Integer productCount;
//    }

    public Ordering toEntity(Member member){
        return Ordering.builder()
                .member(member)
                .build();
    }

}
