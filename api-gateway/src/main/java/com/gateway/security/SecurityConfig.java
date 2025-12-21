package com.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        JwtReactiveAuthenticationManager authManager = new JwtReactiveAuthenticationManager(jwtUtil);
        JwtServerAuthenticationConverter converter = new JwtServerAuthenticationConverter();

        //call auth manager and set context
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authManager);
        jwtFilter.setServerAuthenticationConverter(converter);
        jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                		.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/auth/register", "/auth/login").permitAll()
                        .pathMatchers(HttpMethod.PUT,"/auth/change-password").authenticated()
                        .pathMatchers("/FLIGHT-MICROSERVICE/api/flight/search").permitAll()
                        .pathMatchers(HttpMethod.POST, "/FLIGHT-MICROSERVICE/api/flight").hasRole("ADMIN")
                        .pathMatchers("/BOOKING-MICROSERVICE/api/flight/booking/**").hasRole("USER")
                        .pathMatchers("/BOOKING-MICROSERVICE/api/flight/ticket/**").hasRole("USER")
                        .pathMatchers("/BOOKING-MICROSERVICE/api/flight/booking/history/**").hasRole("USER")
                        .pathMatchers(HttpMethod.DELETE, "/BOOKING-MICROSERVICE/api/flight/booking/cancel/**").hasRole("USER")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .build();
    }
}
