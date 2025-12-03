package com.api.gateway.controller;

import com.api.gateway.dto.request.LoginRequest;
import com.api.gateway.dto.response.CustomerLoginResponse;
import com.api.gateway.dto.response.LoginResponse;
import com.api.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping("/api-gateway/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WebClient.Builder webClientBuilder;
    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<LoginResponse> login(@RequestBody LoginRequest request) {

        return webClientBuilder.build()
                .post()
                .uri(customerServiceUrl + "/internal/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CustomerLoginResponse.class)
                .map(customer -> {

                    String token = jwtUtil.generateToken(
                            customer.getCustomerId(),
                            customer.getEmail()
                    );

                    return LoginResponse.builder()
                            .customerId(customer.getCustomerId())
                            .email(customer.getEmail())
                            .token(token)
                            .build();
                });
    }

    @PostMapping("/logout")
    public Mono<String> logout(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just("No token found");
        }

        String token = authHeader.substring(7);

        // Try to get expiration claim
        Date exp = jwtUtil.getExpiration(token);

        // If token has no expiration → blacklist for 24 hours by default
        long ttlMs;
        if (exp == null) {
            ttlMs = Duration.ofHours(24).toMillis();  // default TTL
        } else {
            ttlMs = exp.getTime() - System.currentTimeMillis();

            // Prevent negative TTL (expired token)
            if (ttlMs < 0) ttlMs = 0;
        }

        return redisTemplate.opsForValue()
                .set("BLACKLIST:" + token, "1", Duration.ofMillis(ttlMs))
                .then(Mono.just("Logged out successfully"));
    }
}
