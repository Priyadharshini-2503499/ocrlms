package com.genc.omnichannel.returns.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.genc.omnichannel.returns.model.ReturnRequest;
import com.genc.omnichannel.returns.model.ReturnStatus;

@Repository
public interface ReturnRepository extends JpaRepository<ReturnRequest, Long> {

    Optional<ReturnRequest> findByOrder_OrderId(Long orderId);
    List<ReturnRequest> findByReturnStatus(ReturnStatus returnStatus);
    boolean existsByOrder_OrderId(Long orderId);
}
