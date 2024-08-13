//package beyondProjectForOrdersystem;
//
//import org.springframework.cloud.gateway.config.GlobalCorsProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
////CorsWebFilter는 reactive이고, 비동기 기반의 프레임워크다
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//
//
//// 해당 코드는, 테스트 목적이고 추후에는 쿠버네티스 기반의 소스를 활용할 예정이다
//@Configuration
//public class CorsConfig {
//    @Bean
//    public CorsWebFilter corsWebFilter(GlobalCorsProperties globalCorsProperties){
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.addAllowedOrigin("http://localhost:8081");
//        corsConfiguration.addAllowedOrigin("*");
//        corsConfiguration.addAllowedOrigin("*");
//        corsConfiguration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // reactive 내부에 있는거 불러오기 필수
//        source.registerCorsConfiguration("/**",corsConfiguration);
//
//        return new CorsWebFilter(source);
//
//    }
//}
