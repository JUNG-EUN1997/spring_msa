package beyondProjectForOrdersystem.ordering.service;

import beyondProjectForOrdersystem.common.configs.FeignConfig;
import beyondProjectForOrdersystem.common.dto.CommonResDto;
import beyondProjectForOrdersystem.ordering.dto.ProductUpdateStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 여기에 url name을 보낼 수 있음
@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductFeign {
    @GetMapping(value = "/product/{id}")
    CommonResDto getProductById(@PathVariable("id") Long id);

    @PutMapping(value = "/product/updatestock")
    void updateProductStock(@RequestBody ProductUpdateStockDto dto);
}
