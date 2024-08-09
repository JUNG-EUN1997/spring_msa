package beyondProjectForOrdersystem.common.service;

import beyondProjectForOrdersystem.member.domain.Role;
import beyondProjectForOrdersystem.member.dto.MemberResDto;
import beyondProjectForOrdersystem.member.dto.MemberSaveReqDto;
import beyondProjectForOrdersystem.member.repository.MemberRepository;
import beyondProjectForOrdersystem.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner { // 프로그램 시작 되자마자 실행되는 comandline
    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public void run(String... args) throws Exception {
         if (memberRepository.findByEmail("admin@test.com").isEmpty()){
             memberService.memberCreate(MemberSaveReqDto.builder()
                     .name("admin")
                     .email("admin@test.com")
                     .password("12341234")
                     .role(Role.ADMIN)
                     .build());
         }

    }

}
