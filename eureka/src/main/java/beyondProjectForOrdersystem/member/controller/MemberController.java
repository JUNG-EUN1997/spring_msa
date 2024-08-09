package beyondProjectForOrdersystem.member.controller;

import beyondProjectForOrdersystem.common.auth.JwtTokenProvider;
import beyondProjectForOrdersystem.common.dto.CommonErrorDto;
import beyondProjectForOrdersystem.common.dto.CommonResDto;
import beyondProjectForOrdersystem.member.domain.Member;
import beyondProjectForOrdersystem.member.dto.MemberLoginDto;
import beyondProjectForOrdersystem.member.dto.MemberRefreshDto;
import beyondProjectForOrdersystem.member.dto.MemberResDto;
import beyondProjectForOrdersystem.member.dto.MemberSaveReqDto;
import beyondProjectForOrdersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//@Controller
@RestController
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider,@Qualifier("2") RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }


    @PostMapping("/member/create")
    public ResponseEntity<?> memberCreate(@Valid @RequestBody MemberSaveReqDto dto){ // dto단에 validation을 체크하려면
        Member member = memberService.memberCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,
                "member is successfully create", member.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }


//    어드민만 가능하다는 표시 >> ... SimpleGrantedAuthority("ROLE_"+claims.get("role"))); 부분에 "Role_" 이게 있어야 작동한다
//    admin만 회원목록 전체조회 가능
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/list")
    public ResponseEntity<?> memberList(
            @PageableDefault(page = 0, size=10, sort = "createdTime", direction = Sort.Direction.DESC )
            Pageable pageable){

        Page<MemberResDto> memberListResDtos =  memberService.memberList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,
                "member are found", memberListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    마이페이지 : 본인은 본인정보만 조회 가능
    @GetMapping("/member/myinfo")
    public ResponseEntity<?> memberMyinfo(){

        MemberResDto memberResDto = memberService.memberMyinfo();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member are found", memberResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto){
//        email, password가 일치한지 검증
        Member member = memberService.login(dto);

//        일치할경우 accessToken 생성 (accessToken = JWT Token)
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());


//        redis에 email과 rt를 key:value로 하여 저장
        redisTemplate.opsForValue().set(member.getEmail(),refreshToken, 240, TimeUnit.HOURS); // 240시간
//        생성된 토큰을 CommonResDto에 담아 사용자에게 return
        Map<String,Object> loginInfo = new HashMap<>();
        loginInfo.put("id",member.getId());
        loginInfo.put("token",jwtToken);
        loginInfo.put("refresh",refreshToken);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,"login is successful",loginInfo);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto){
        String refreshToken = dto.getRefreshToken();
        Claims claims = null;
        try{

//            ⭐검증 1.⭐ 코드를 통해 rt 검증
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(refreshToken).getBody();
        }catch (Exception e){
            e.getMessage();
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value()
                    ,"invalid refresh token"),HttpStatus.UNAUTHORIZED);
        }

        String email = claims.getSubject();
        String role = claims.get("role").toString();

//        ⭐검증 2.⭐ redis를 조회하여 rt 추가검증
        Object obj = redisTemplate.opsForValue().get(email);
        if (obj == null || !obj.toString().equals(refreshToken)){
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value()
                    ,"invalid refresh token"),HttpStatus.UNAUTHORIZED);
        }

        String newAt = jwtTokenProvider.createToken(email,role);

//        생성된 토큰을 CommonResDto에 담아 사용자에게 return
        Map<String,Object> into = new HashMap<>();
        into.put("token",newAt);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,"at is renewed",into);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }



}
