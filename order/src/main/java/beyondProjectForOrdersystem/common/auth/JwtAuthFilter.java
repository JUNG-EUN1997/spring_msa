package beyondProjectForOrdersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.security.sasl.AuthenticationException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// JWT TOKEN : JSON WEB TOKEN
// JWT를 통한 인증 검증 토큰 사용할 예정
@Component
@Slf4j
public class JwtAuthFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String secretKey;



    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String bearerToken = ((HttpServletRequest)request).getHeader("Authorization");
        try{
            if (bearerToken != null){
//            token 관례적으로 [Bearer ]로 시작하는 문구를 넣어서 요청
                if(!bearerToken.substring(0,7).equals("Bearer ")){
                    throw new AuthenticationException("Bearer 형식이 아닙니다.");
                }
                String token = bearerToken.substring(7);
//            token 검증 및 claims(사용자 정보) 추출
//            token 생성 시에 사용한 secret키값을 넣어 토큰 검증에 사용
//                아래 Claims 줄이 검증코드! (한 줄로 끝남)
                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
                //setSigningKey 자리에는 secretKey를 써야함


//            Authentication객체 생성(UserDetails객체도 필요)
                List<GrantedAuthority> authorities = new ArrayList<>(); // 유저의 권한을 불러오기 위한 객체
//            User 권한이 1개 이상일 수 있기 때문에 List 형태로 불러온다.

                authorities.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role")));
                UserDetails userDetails = new User(claims.getSubject(),"", authorities);

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
//            나 자신의 정보가 1인지 2인지 filetring하는 것은 > user의 정보는 authentication에 들어가있다.
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }

//        filterchain에서 그 다음 filtering으로 넘어가도록 하는 메서드
            chain.doFilter(request, response);
        }catch (Exception e){
            log.error(e.getMessage());

//            filter 차원의 소스라, return해서 돌아가 오류를 출력할 수 없다.
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("token 에러");
        }
    }


}
