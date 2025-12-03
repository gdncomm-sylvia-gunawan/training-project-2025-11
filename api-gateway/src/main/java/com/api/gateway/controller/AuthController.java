package com.api.gateway.controller;

import com.api.gateway.dto.request.LoginRequest;
import com.api.gateway.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    private final WebClient webClient;

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest request) {
        return webClient.post()
                .uri(customerServiceUrl + "/internal/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }
}
