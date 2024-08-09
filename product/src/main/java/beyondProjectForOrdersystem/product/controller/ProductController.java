package beyondProjectForOrdersystem.product.controller;

import beyondProjectForOrdersystem.common.dto.CommonResDto;
import beyondProjectForOrdersystem.product.domain.Product;
import beyondProjectForOrdersystem.product.dto.ProductResDto;
import beyondProjectForOrdersystem.product.dto.ProductSaveReqDto;
import beyondProjectForOrdersystem.product.dto.ProductSearchDto;
import beyondProjectForOrdersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
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


}
