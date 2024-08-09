package beyondProjectForOrdersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
//    at용
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int expiration;

//    rt용
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;


    public String createToken(String email, String role){
//        Claims는 사용자 정보이자 페이로드 정보
        Claims claims = Jwts.claims().setSubject(email);
//        setSubject :: 세팅하고자 하는 주요한 값을 넣기

        claims.put("role",role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 생성시간

//                30분동안 유효하도록 설정해놓은 것
                .setExpiration(new Date(now.getTime() + expiration*60*1000L)) // 만료시간 (30분, 밀리초단위) / 30(분) * 60(초) * 1000(밀리초)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return token;
    }


    public String createRefreshToken(String email, String role){
        Claims claims = Jwts.claims().setSubject(email);

        claims.put("role",role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration*60*1000L))
                .signWith(SignatureAlgorithm.HS256, secretKeyRt)
                .compact();
        return token;
    }


}
