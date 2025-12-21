//A Gateway filter that forwards authenticated user details (username, role) as HTTP headers to downstream microservices.
package com.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserForwardingFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String username = exchange.getAttribute("username");
        String role = exchange.getAttribute("role");

        if (username == null) {
            return chain.filter(exchange);
        }

        var mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Name", username)
                .header("X-User-Role", role)
                .build();

        var mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange);
    }
}
