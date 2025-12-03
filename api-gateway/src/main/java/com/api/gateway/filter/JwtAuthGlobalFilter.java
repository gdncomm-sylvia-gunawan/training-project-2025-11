//package com.api.gateway.filter;
//
//import com.api.gateway.security.JwtUtil;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
//@Component
//public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {
//
//    private final JwtUtil jwtUtil;
//    private final AntPathMatcher matcher = new AntPathMatcher();
//
//    // add any public patterns you want here (login + swagger + api-docs)
//    private final List<String> whitelist = List.of(
//            "/api-gateway/login",
//            "/api-gateway/logout",
//            "/api-gateway/swagger-ui/**",
//            "/api-gateway/v3/api-docs/**",
//            "/webjars/**",
//            "/swagger-ui/**",
//            "/actuator/**"
//    );
//
//    public JwtAuthGlobalFilter(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        String path = exchange.getRequest().getPath().value();
//
//        // skip whitelist
//        for (String p : whitelist) {
//            if (matchPattern(p, path)) {
//                return chain.filter(exchange);
//            }
//        }
//
//        // check Authorization header
//        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        if (auth == null || !auth.startsWith("Bearer ")) {
//            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//        String token = auth.substring(7);
//
//        if (!jwtUtil.isValid(token)) {
//            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        // get subject (customerId) and forward header
//        String subject = jwtUtil.getSubject(token);
//        ServerHttpRequest mutated = exchange.getRequest().mutate()
//                .header("X-Auth-CustomerId", subject)
//                .build();
//        ServerWebExchange mutatedExchange = exchange.mutate().request(mutated).build();
//
//        return chain.filter(mutatedExchange);
//    }
//
//    private boolean matchPattern(String pattern, String path) {
//        // Ant-style pattern matching
//        return matcher.match(pattern, path);
//    }
//
//    @Override
//    public int getOrder() {
//        // should run early
//        return -100;
//    }
//}
