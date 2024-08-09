package beyondProjectForOrdersystem.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

// 타 엔티티에서 사용가능한 형태로 만드는 어노테이션 : @Entity는 붙일필요 X :: 테이블이 따로 만들어짐
@Embeddable // embed > 삽입하다
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String city;
    private String street;
    private String zipcode;
}
