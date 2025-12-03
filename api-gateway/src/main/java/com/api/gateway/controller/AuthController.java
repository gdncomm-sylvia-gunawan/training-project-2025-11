package com.api.gateway.controller;

import com.api.gateway.dto.request.LoginRequest;
import com.api.gateway.dto.response.CustomerLoginResponse;
import com.api.gateway.dto.response.LoginResponse;
import com.api.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api-gateway/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WebClient.Builder webClientBuilder;
    private final JwtUtil jwtUtil;

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
}
