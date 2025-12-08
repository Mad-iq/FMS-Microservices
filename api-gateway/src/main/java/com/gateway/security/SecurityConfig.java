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

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        JwtReactiveAuthenticationManager authManager = new JwtReactiveAuthenticationManager(jwtUtil);
        JwtServerAuthenticationConverter converter = new JwtServerAuthenticationConverter();

        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authManager);
        jwtFilter.setServerAuthenticationConverter(converter);
        jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/auth/**").permitAll()
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
