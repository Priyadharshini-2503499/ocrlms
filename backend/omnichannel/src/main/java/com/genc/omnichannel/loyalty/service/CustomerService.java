package com.genc.omnichannel.loyalty.service;

import com.genc.omnichannel.loyalty.dto.CustomerDTO;
import com.genc.omnichannel.loyalty.model.Customer;
import com.genc.omnichannel.loyalty.model.LoyaltyTier;
import com.genc.omnichannel.loyalty.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFullName(customer.getFullName());
        dto.setEmailId(customer.getEmailId());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setLoyaltyPoints(customer.getLoyaltyPoints());
        dto.setLoyaltyTier(customer.getLoyaltyTier());
        return dto;
    }

    private Customer convertToEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setCustomerId(dto.getCustomerId());
        customer.setFullName(dto.getFullName());
        customer.setEmailId(dto.getEmailId());
        customer.setPhoneNumber(dto.getPhoneNumber());

        customer.setLoyaltyPoints(dto.getLoyaltyPoints() != null ? dto.getLoyaltyPoints() : 0);
        customer.setLoyaltyTier(dto.getLoyaltyTier() != null ? dto.getLoyaltyTier() : LoyaltyTier.SILVER);
        return customer;
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomerById(Long customerId) {
        Customer customer =customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer record missing with ID: " + customerId));
        return convertToDTO(customer);
    }

    @Transactional
    public CustomerDTO registerCustomer(CustomerDTO customerDTO) {
        if (customerRepository.existsByEmailId(customerDTO.getEmailId())) {
            throw new IllegalArgumentException("Email ID is already registered.");
        }
        Customer customer = convertToEntity(customerDTO);
        customer.setLoyaltyPoints(0);
        customer.setLoyaltyTier(LoyaltyTier.SILVER);
        return convertToDTO(customerRepository.save(customer));
    }


    @Transactional
    public Customer updateLoyaltyTier(Customer customer) {
        int points = customer.getLoyaltyPoints();
        if (points >= 2000) {
            customer.setLoyaltyTier(LoyaltyTier.PLATINUM);
        } else if (points >= 1000) {
            customer.setLoyaltyTier(LoyaltyTier.GOLD);
        } else {
            customer.setLoyaltyTier(LoyaltyTier.SILVER);
        }
        return customerRepository.save(customer);
    }
    @Transactional
    public CustomerDTO redeemPoints(Long customerId, int pointsToRedeem) {
        Customer customer =  customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer record missing with ID: " + customerId));
        if (customer.getLoyaltyPoints() < pointsToRedeem) {
            throw new IllegalStateException("Insufficient points balance.");
        }
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() - pointsToRedeem);
        Customer updatedCustomer = updateLoyaltyTier(customer);
        return convertToDTO(updatedCustomer);
    }
    @Transactional
    public CustomerDTO accruePointsFromPurchase(Long customerId, double transactionAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer record missing with ID: " + customerId));
        int pointsEarned = (int) Math.floor(transactionAmount);

        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pointsEarned);
        Customer updatedCustomer = updateLoyaltyTier(customer);
        return convertToDTO(updatedCustomer);
    }
    @Transactional
    public void deductPointsFromRefund(Long customerId, double refundAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer record missing with ID: " + customerId));

        // Calculate original points earned from the refunded transaction amount
        int pointsToDeduct = (int) Math.floor(refundAmount);
        int updatedPointsBalance = customer.getLoyaltyPoints() - pointsToDeduct;

        // Business Rule Guardrail: Loyalty balance shouldn't drop below 0 points baseline
        customer.setLoyaltyPoints(Math.max(0, updatedPointsBalance));

        // Re-evaluate and demote tier level back down if criteria is no longer met
        updateLoyaltyTier(customer);


    }
}