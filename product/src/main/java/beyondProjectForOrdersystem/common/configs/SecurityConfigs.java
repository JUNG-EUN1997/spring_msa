package beyondProjectForOrdersystem.common.configs;

import beyondProjectForOrdersystem.common.auth.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity // security를 위한 소스코드다 라는 명시
@EnableGlobalMethodSecurity(prePostEnabled = true) // pre : 사전검증 , post : 사후검증
@Configuration
public class SecurityConfigs {
    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    public SecurityConfigs(JwtAuthFilter jwtAuthFilter){
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf().disable()
                .cors().and() // CORS 활성화
                .httpBasic().disable()
                .authorizeRequests()

                    // (antMatchers) 예외 url 넣기 : 로그인하지 않아도 들어갈 수 있는
                    .antMatchers("/member/create", "/", "/doLogin","/refresh-token"
                            , "/product/list", "/product/config/test")
                    .permitAll()
                .anyRequest().authenticated()
                .and()

//                세션로그인이 아닌, stateless한 token login을 사용하겠다는 의미
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // STATELESS : 상태가 없는.
                .and()

//                로그인 시, 사용자는 서버로부터 토큰을 발급받고
//                매 요청마다 해당 토큰을 http header에 넣어 요청함.
//                아래 코드는 매 요청마다 사용자로부터 받아온 토큰이 정상인지 검증하는 코드
//                      세션은 만들어놓고, 서버가 가지고있어서 갖고있는지 여부만 확인하면 된다.
//                          근데 토큰은 서버가 갖고있찌 않아서, 검증로직이 따로 필요하다.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // 소스가 너무 길어서 따로 파일구분

                .build();
    }
}
