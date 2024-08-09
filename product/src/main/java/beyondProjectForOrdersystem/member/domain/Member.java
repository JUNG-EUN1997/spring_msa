package beyondProjectForOrdersystem.member.domain;

import beyondProjectForOrdersystem.common.domain.Address;
import beyondProjectForOrdersystem.common.domain.BaseTimeEntity;
import beyondProjectForOrdersystem.member.dto.MemberResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // 해당 어노테이션 사용하지 않으면 숫자로 들어감
    @Builder.Default
    private Role role = Role.USER;

    @Embedded // @Embeddable 선언한 객체를 사용하겠다는 어노테이션
    private Address address;

    public MemberResDto fromEntity(){
        return MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .build();
    }

}
