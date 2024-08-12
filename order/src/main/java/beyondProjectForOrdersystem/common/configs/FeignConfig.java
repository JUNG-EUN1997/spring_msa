package beyondProjectForOrdersystem.common.configs;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor(){

//        모든 feign 요청에 전역적으로 token을 세팅
        return request -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
            request.header(HttpHeaders.AUTHORIZATION, token);


        };
    }
}
