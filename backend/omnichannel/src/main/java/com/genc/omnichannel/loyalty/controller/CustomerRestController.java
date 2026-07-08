package com.genc.omnichannel.loyalty.controller;

import com.genc.omnichannel.loyalty.dto.CustomerDTO;
import com.genc.omnichannel.loyalty.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRestController.class);
    private final CustomerService customerService;

    public CustomerRestController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        logger.info("REST API Request: Fetching all customer registry entries");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        logger.info("REST API Request: Fetching profile metrics for customer ID: {}", id);
        try {
            return ResponseEntity.ok(customerService.getCustomerById(id));
        } catch (IllegalArgumentException ex) {
            logger.warn("REST API Failed: Customer profile ID: {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> registerCustomer(@RequestBody CustomerDTO customerDTO) {
        logger.info("REST API Request: Registering new customer entry for email: {}", customerDTO.getEmailId());
        try {
            CustomerDTO saved = customerService.registerCustomer(customerDTO);
            logger.info("REST API Success: Customer registered successfully with allocated ID: {}", saved.getCustomerId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            logger.warn("REST API Failed: Registration rejected for email: {}. Reason: {}", customerDTO.getEmailId(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/redeem")
    public ResponseEntity<?> redeemPoints(@PathVariable Long id,
                                          @RequestParam int points) {
        logger.info("REST API Request: Processing point redemption event for customer ID: {}, Points requested: {}", id, points);
        try {
            CustomerDTO updated = customerService.redeemPoints(id, points);
            logger.info("REST API Success: Redeemed {} points successfully for customer ID: {}. Remaining Balance: {}", points, id, updated.getLoyaltyPoints());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            logger.warn("REST API Failed: Redemption event blocked for customer ID: {}. Reason: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/add-points")
    public ResponseEntity<?> addPoints(@PathVariable Long id,
                                       @RequestParam int points) {
        logger.info("REST API Request: Manual point accrual submission for customer ID: {},Points: {}", id, points);
        try {
            CustomerDTO updated = customerService.accruePointsFromPurchase(id, (double) points);
            logger.info("REST API Success: Added {} points to customer ID: {}. New Balance: {}, Tier Status: {}", points, id, updated.getLoyaltyPoints(), updated.getLoyaltyTier());
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            logger.error("Critical error processing point accrual for customer ID: {}", id, ex);            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}

