package beyondProjectForOrdersystem.member.service;

import beyondProjectForOrdersystem.member.domain.Member;
import beyondProjectForOrdersystem.member.dto.MemberLoginDto;
import beyondProjectForOrdersystem.member.dto.MemberResDto;
import beyondProjectForOrdersystem.member.dto.MemberSaveReqDto;
import beyondProjectForOrdersystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Transactional
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder){
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member memberCreate(MemberSaveReqDto dto){
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 email 입니다.");
        }
        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
        memberRepository.save(member);
        return member;
    }

    public Page<MemberResDto> memberList(Pageable pageable){
        Page<Member> memberLists = memberRepository.findAll(pageable);
        Page<MemberResDto> memberListResDtos = memberLists.map(a->a.fromEntity());
        return memberListResDtos;
    }

    public MemberResDto memberMyinfo(){
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));

        MemberResDto memberResDto = member.fromEntity();
        return memberResDto;
    }

    public Member login(MemberLoginDto dto){
//        email 존재여부
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));

//        password 일치여부
        if (!passwordEncoder.matches(dto.getPassword(),member.getPassword())){
//            사용자가 현재 입력한 비밀번호를 encode하여 해당 비밀번호와, DB상의 비밀번호가 같은지 비교

            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;

    }

}
