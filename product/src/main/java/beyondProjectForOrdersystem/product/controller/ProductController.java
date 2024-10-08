package beyondProjectForOrdersystem.product.controller;

import beyondProjectForOrdersystem.common.dto.CommonResDto;
import beyondProjectForOrdersystem.product.domain.Product;
import beyondProjectForOrdersystem.product.dto.ProductResDto;
import beyondProjectForOrdersystem.product.dto.ProductSaveReqDto;
import beyondProjectForOrdersystem.product.dto.ProductSearchDto;
import beyondProjectForOrdersystem.product.dto.ProductUpdateStockDto;
import beyondProjectForOrdersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// @RefreshScope 사용 시, 아래 스프링 Bean은 실시간 config 변경사항의 대상이 된다.
@RefreshScope
@RestController
public class ProductController {
    private final ProductService productService;

    @Value("${message.hello}")
    private String helloworld;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping("/product/config/test")
    public String configTest(){
        return helloworld;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/product/create")
    public ResponseEntity<?> productCreate(@ModelAttribute ProductSaveReqDto dto){
        Product product = productService.productCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK
                ,"product is successfuly created", product.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/product/list")
    public ResponseEntity<?> productList(ProductSearchDto searchDto, Pageable pageable){
        Page<ProductResDto> products = productService.productList(searchDto, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "product list are successfully return", products);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id) {
        ProductResDto products = productService.productDetail(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상조회 완료", products);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PutMapping("/product/updatestock")
    public ResponseEntity<?> productStockUpdate(@RequestBody ProductUpdateStockDto dto) {
        Product product = productService.productStockUpdate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상 업데이트 완료", product.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
