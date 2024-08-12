package beyondProjectForOrdersystem.product.service;

import beyondProjectForOrdersystem.common.service.StockInventoryService;
import beyondProjectForOrdersystem.product.domain.Product;
import beyondProjectForOrdersystem.product.dto.ProductResDto;
import beyondProjectForOrdersystem.product.dto.ProductSaveReqDto;
import beyondProjectForOrdersystem.product.dto.ProductSearchDto;
import beyondProjectForOrdersystem.product.dto.ProductUpdateStockDto;
import beyondProjectForOrdersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    @Autowired
    public ProductService(ProductRepository productRepository, S3Client s3Client, StockInventoryService stockInventoryService) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.stockInventoryService = stockInventoryService;
    }

    public Product productCreate(ProductSaveReqDto dto){
        MultipartFile image = dto.getProductImage();
        Product product;
        try{
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            Path path = Paths.get("C:/Users/rro06/OneDrive/Desktop/tmp/"
                    , product.getId() + "_" + image.getOriginalFilename());
            Files.write(path,bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            product.updateImagePath(path.toString());

//            상품 등록 시, redis에 등록 여부 체그
            if(dto.getName().contains("sale")){
                stockInventoryService.increaseStock(product.getId(),dto.getStockQuantity());
            }
        }catch (IOException e){
            throw new RuntimeException("이미지 저장 실패");
        }
        return product;
    }

//    aws s3 버킷에 파일을 넣을 것 인데, 업로드를 위한 권한을 어떻게 줄 것인가?
//          1) id + pw  > 권한이 너무 많아서 X
//          2) s3에 권한이 있는 특정 계정 생성함
    public Product productAwsCreate(ProductSaveReqDto dto){
        MultipartFile image = dto.getProductImage();
        Product product;
        try{
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            String fileName = product.getId() + "_" + image.getOriginalFilename();
            Path path = Paths.get("C:/Users/rro06/OneDrive/Desktop/tmp/"
                    , fileName);
            /*
            * 1. FRONT > BACK 에서 줄 때 AWS에서 BYTE를 바로 올리는 기능을 지원해주지 않음
            * 2. 따라서 BYTE를 다시 파일로 변환
            * 3. 그 파일을 AWS로 업로드 하는 방향으로 진행
            * */

//            local PC에 임시 저장
            Files.write(path,bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

//            AWS에 PC에 저장된 파일을 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            String s3FiltPath = s3Client.utilities()
                    .getUrl(a->a.bucket(bucket).key(fileName)) // 해당 fileName으로 bucket 안에 있는 path를 찾아옴
                    .toExternalForm();

            product.updateImagePath(s3FiltPath);

        }catch (IOException e){
            throw new RuntimeException("이미지 저장 실패");
        }
        return product;
    }


    public Page<ProductResDto> productList(ProductSearchDto searchDto, Pageable pageable){
//        검색을 위해 Specification 객체 사용
//        Specification 객체는 복잡한 쿼리를 명세를 이용하여 정의하는 방식, 쿼리를 쉽게 생성

        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                ~~할 때 어떤 쿼리를 넣겠다를 명시 해준다.
                List<Predicate> predicates = new ArrayList<>();
                if(searchDto.getSearchName() != null){
//                    CriteriaBuilder : 쿼리를 생성하기위한 객체
//                    root : entity의 속성을 접근하기위한 객체
                    predicates.add(criteriaBuilder.like(root.get("name"),"%"+searchDto.getSearchName()));
                }
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.like(root.get("category"),searchDto.getCategory()));
                }

                Predicate[] predicateArr = new Predicate[predicates.size()];
                for (int i = 0; i < predicateArr.length; i++) {
                    predicateArr[i] = predicates.get(i);
                }

                System.out.println(predicateArr);

//                위 2개의 쿼리 조건문을 and조건으로 연결
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };

        Page<Product> listProduct = productRepository.findAll(specification, pageable);
        Page<ProductResDto> productResDtoPage = listProduct.map(a->a.fromEntity());
        return productResDtoPage;
    }

    public ProductResDto productDetail(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        ProductResDto productResDto = product.fromEntity();
        return productResDto;
    }

    public Product productStockUpdate(ProductUpdateStockDto dto){
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        product.updateStockQuantity("minus", dto.getProductQuantity());
        return product;
    }

}
