package com.genc.omnichannel.loyalty.repository;

import com.genc.omnichannel.loyalty.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmailId(String emailId);
}