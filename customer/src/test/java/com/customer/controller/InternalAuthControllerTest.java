package com.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.customer.entity.Customer;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalAuthController.class)
class InternalAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerAuthRepository customerAuthRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFindByEmail_ReturnsCustomerAuth() throws Exception {
        // Arrange
        UUID custId = UUID.randomUUID();

        Customer customer = Customer.builder()
                .id(custId)
                .name("Test User")
                .email("test@gmail.com")
                .phone("12345")
                .address("Address")
                .createdAt(LocalDateTime.now())
                .build();

        CustomerAuth auth = CustomerAuth.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .email("test@gmail.com")
                .passwordHash("hashed-password")
                .build();

        when(customerAuthRepository.findByEmailIgnoreCase("test@gmail.com"))
                .thenReturn(Optional.of(auth));

        // Act + Assert
        mockMvc.perform(get("/internal/auth/customer")
                        .param("email", "test@gmail.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(custId.toString()))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.passwordHash").value("hashed-password"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testFindByEmail_NotFound() throws Exception {
        // Arrange
        when(customerAuthRepository.findByEmailIgnoreCase("notfound@mail.com"))
                .thenReturn(Optional.empty());

        // Act + Assert
        mockMvc.perform(get("/internal/auth/customer")
                        .param("email", "notfound@mail.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
