package com.gateway.security;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;
    private final ServerWebExchange exchange;

    public JwtReactiveAuthenticationManager(JwtUtil jwtUtil, ServerWebExchange exchange) {
        this.jwtUtil = jwtUtil;
        this.exchange = exchange;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();

        if (token == null || !jwtUtil.validateToken(token)) {
            return Mono.empty();
        }

        Claims claims = jwtUtil.extractAllClaims(token);

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        Boolean pwdExpired = claims.get("pwd_expired", Boolean.class);

        String path = exchange.getRequest().getPath().value();
        boolean isChangePasswordEndpoint =
                path.equals("/auth/change-password");
        if (Boolean.TRUE.equals(pwdExpired) && !isChangePasswordEndpoint) {
            return Mono.error(new AccessDeniedException("PASSWORD_EXPIRED"));
        }

        return Mono.just(
            new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
            )
        );
    }
}
