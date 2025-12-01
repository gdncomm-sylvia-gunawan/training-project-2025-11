package com.customer.service.impl;

import com.customer.dto.CreateCustomerRequest;
import com.customer.dto.CustomerResponse;
import com.customer.dto.UpdateCustomerRequest;
import com.customer.entity.Customer;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import com.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAuthRepository customerAuthRepository;

    @InjectMocks
    private CustomerServiceImpl service;

    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        mockCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Sylvia")
                .email("sylvia@mail.com")
                .phone("08123")
                .address("Gading Serpong")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ----------------------------
    // Test createCustomer
    // ----------------------------
    @Test
    void testCreateCustomer() {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("Sylvia");
        req.setEmail("sylvia@mail.com");
        req.setPhone("08123");
        req.setAddress("Gading Serpong");
        req.setPassword("mypassword");

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(mockCustomer);

        when(customerAuthRepository.save(any(CustomerAuth.class)))
                .thenReturn(null);

        Customer saved = service.createCustomer(req);

        assertNotNull(saved);
        assertEquals("Sylvia", saved.getName());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(customerAuthRepository, times(1)).save(any(CustomerAuth.class));
    }

    // ----------------------------
    // Test updateCustomer (success)
    // ----------------------------
    @Test
    void testUpdateCustomer() {
        UUID id = mockCustomer.getId();

        UpdateCustomerRequest req = new UpdateCustomerRequest();
        req.setName("Updated Name");
        req.setPhone("08122");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(mockCustomer));

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(mockCustomer);

        Customer updated = service.updateCustomer(id, req);

        assertEquals("Updated Name", updated.getName());
        assertEquals("08122", updated.getPhone());
    }

    // ----------------------------
    // Test updateCustomer (not found)
    // ----------------------------
    @Test
    void testUpdateCustomer_NotFound() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updateCustomer(id, new UpdateCustomerRequest()));
    }

    // ----------------------------
    // Test getAllCustomers
    // ----------------------------
    @Test
    void testGetAllCustomers() {
        Page<Customer> page = new PageImpl<>(List.of(mockCustomer));

        when(customerRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<Customer> result = service.getAllCustomers(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Sylvia", result.getContent().get(0).getName());
    }

    // ----------------------------
    // Test deleteCustomer (existing)
    // ----------------------------
    @Test
    void testDeleteCustomer_Exists() {
        UUID id = mockCustomer.getId();

        when(customerRepository.existsById(id))
                .thenReturn(true);

        service.deleteCustomer(id);

        verify(customerAuthRepository, times(1)).deleteByCustomerId(id);
        verify(customerRepository, times(1)).deleteById(id);
    }

    // ----------------------------
    // Test deleteCustomer (not exists)
    // Should do nothing
    // ----------------------------
    @Test
    void testDeleteCustomer_NotExists() {
        UUID id = UUID.randomUUID();

        when(customerRepository.existsById(id))
                .thenReturn(false);

        service.deleteCustomer(id);

        verify(customerAuthRepository, never()).deleteByCustomerId(any());
        verify(customerRepository, never()).deleteById(any());
    }

    // ----------------------------
    // Test getById
    // ----------------------------
    @Test
    void testGetById() {
        UUID id = mockCustomer.getId();

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(mockCustomer));

        CustomerResponse response = service.getById(id);

        assertEquals(mockCustomer.getId(), response.getId());
        assertEquals("Sylvia", response.getName());
        assertEquals("Gading Serpong", response.getAddress());
    }

    // ----------------------------
    // Test getById not found
    // ----------------------------
    @Test
    void testGetById_NotFound() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getById(id));
    }

    // ----------------------------
    // Test getAllCustomersByName
    // ----------------------------
    @Test
    void testGetAllCustomersByName() {
        Page<Customer> page = new PageImpl<>(List.of(mockCustomer));

        when(customerRepository.findByNameContainingIgnoreCase(eq("syl"), any(Pageable.class)))
                .thenReturn(page);

        Page<Customer> result = service.getAllCustomersByName("syl", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    // ----------------------------
    // Test exists
    // ----------------------------
    @Test
    void testExists() {
        UUID id = mockCustomer.getId();

        when(customerRepository.existsById(id))
                .thenReturn(true);

        assertTrue(service.exists(id));
    }
}
