package beyondProjectForOrdersystem.member.dto;

import beyondProjectForOrdersystem.common.domain.Address;
import beyondProjectForOrdersystem.member.domain.Member;
import beyondProjectForOrdersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberSaveReqDto {
    private String name;
    @NotEmpty(message = "email is essential")
    private String email;
    @NotEmpty(message = "password is essential")
    @Size(min=8,message = "password minimun length is 8")
    private String password;
//    private String city;
//    private String street;
//    private String zipcode;
    private Address address; // 엔티티가 아님 + oneToMany 가 아니어서 순환참조가 아님!

    @Builder.Default
    private Role role = Role.USER;;

    public Member toEntity(String password){
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(password)
                .role(this.role)
                .address(address)
                .build();
    }
}
