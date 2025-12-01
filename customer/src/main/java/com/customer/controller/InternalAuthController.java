package com.customer.controller;

import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final CustomerAuthRepository customerAuthRepository;

    @GetMapping("/customer")
    public ResponseEntity<?> findByEmail(@RequestParam String email) {
        CustomerAuth auth = customerAuthRepository.findByEmailIgnoreCase(email)
                .orElse(null);

        if (auth == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "customerId", auth.getCustomer().getId(),
                        "email", auth.getEmail(),
                        "passwordHash", auth.getPasswordHash(),
                        "status", "ACTIVE"
                )
        );
    }
}
