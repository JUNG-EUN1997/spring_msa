package beyondProjectForOrdersystem.ordering.service;

import org.springframework.cloud.openfeign.FeignClient;

// 여기에 url name을 보낼 수 있음
@FeignClient(name = "product-service", configuration = Fe)
public interface ProductFeign {

}
