package beyondProjectForOrdersystem.ordering.controller;

import beyondProjectForOrdersystem.common.dto.CommonResDto;
import beyondProjectForOrdersystem.ordering.domain.Ordering;
import beyondProjectForOrdersystem.ordering.dto.OrderListResDto;
import beyondProjectForOrdersystem.ordering.dto.OrderSaveReqDto;
import beyondProjectForOrdersystem.ordering.dto.OrderUpdateReqDto;
import beyondProjectForOrdersystem.ordering.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class OrderingController {
    private final OrderingService orderingService;

    @Autowired
    public OrderingController(OrderingService orderingService){
        this.orderingService = orderingService;
    }


    @PostMapping("/order/create")
    public ResponseEntity<?> orderCreate(@RequestBody List<OrderSaveReqDto> dto){
        Ordering ordering = orderingService.orderFeignClientCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED,"정상 주문 완료", ordering.getId());
        return new ResponseEntity<>(commonResDto,HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/list")
    public ResponseEntity<?> orderList(Pageable pageable){
        Page<OrderListResDto> orderlist = orderingService.orderList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,"정상 조회 완료",orderlist);
        return new ResponseEntity<>(commonResDto,HttpStatus.OK);
    }

//    내 주문만 볼 수 있는  myOrders : order/myorders
    @GetMapping("/order/myorders")
    public ResponseEntity<?> myOrderList(Pageable pageable){
        Page<OrderListResDto> myOrderList = orderingService.myOrderList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,"정상 조회 완료",myOrderList);
        return new ResponseEntity<>(commonResDto,HttpStatus.OK);
    }

//    admin 사용자만 할 수 있는 주문 취소
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/order/{id}/cancel")
    public ResponseEntity<?> adminOrderCancel(@PathVariable Long id){
        Ordering ordering = orderingService.orderCancel(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,"정상 취소 완료",ordering.getId());
        return new ResponseEntity<>(commonResDto,HttpStatus.OK);
    }

}
