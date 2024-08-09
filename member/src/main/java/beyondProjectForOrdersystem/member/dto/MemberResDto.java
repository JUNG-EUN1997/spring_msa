package beyondProjectForOrdersystem.member.dto;

import beyondProjectForOrdersystem.common.domain.Address;
import beyondProjectForOrdersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberResDto {
    private Long id;
    private String name;
    private String email;
    private Address address;

}
