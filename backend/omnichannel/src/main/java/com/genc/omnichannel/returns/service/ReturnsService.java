package com.genc.omnichannel.returns.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import com.genc.omnichannel.loyalty.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.genc.omnichannel.order.model.Order;
import com.genc.omnichannel.order.repository.OrderRepository;
import com.genc.omnichannel.returns.dto.InitiateReturnRequest;
import com.genc.omnichannel.returns.dto.ReturnResponse;
import com.genc.omnichannel.returns.mapper.ReturnMapper;
import com.genc.omnichannel.returns.model.ReturnRequest;
import com.genc.omnichannel.returns.model.ReturnStatus;
import com.genc.omnichannel.returns.repository.ReturnRepository;


@Service
public class ReturnsService {

    private static final int RETURN_WINDOW_DAYS = 30;

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    public ReturnsService(ReturnRepository returnRepository, OrderRepository orderRepository, CustomerService customerService) {
        this.returnRepository = returnRepository;
        this.orderRepository = orderRepository;
        this.customerService = customerService;
    }


    @Transactional
    public ReturnResponse initiateReturn(InitiateReturnRequest request) {
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("Order id is required to initiate a return.");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found with id: " + request.getOrderId()));

        if (returnRepository.existsByOrder_OrderId(order.getOrderId())) {
            throw new IllegalStateException(
                    "A return request already exists for order " + order.getOrderId() + ".");
        }

        ReturnRequest entity = ReturnMapper.toEntity(request);
        entity.setOrder(order);
        entity.setRefundAmount(order.getTotalAmount());

        LocalDate requestDate = entity.getRequestDate();
        if (requestDate == null) {
            requestDate = LocalDate.now();
            entity.setRequestDate(requestDate);
        }
        if (!isWithinReturnWindow(requestDate)) {
            throw new IllegalArgumentException(
                    "Return request date is outside the allowed " + RETURN_WINDOW_DAYS + "-day window.");
        }

        entity.setReturnStatus(ReturnStatus.REQUESTED);
        return ReturnMapper.toResponse(returnRepository.save(entity));
    }

    @Transactional
    public ReturnResponse approveReturn(Long returnId) {
        ReturnRequest entity = findEntity(returnId);
        if (entity.getReturnStatus() != ReturnStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Only returns in REQUESTED state can be approved. Current state: "
                            + entity.getReturnStatus());
        }
        entity.setReturnStatus(ReturnStatus.APPROVED);
        return ReturnMapper.toResponse(returnRepository.save(entity));
    }

    @Transactional
    public ReturnResponse rejectReturn(Long returnId) {
        ReturnRequest entity = findEntity(returnId);
        if (entity.getReturnStatus() != ReturnStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Only returns in REQUESTED state can be rejected. Current state: "
                            + entity.getReturnStatus());
        }
        entity.setReturnStatus(ReturnStatus.REJECTED);
        return ReturnMapper.toResponse(returnRepository.save(entity));
    }

    @Transactional
    public ReturnResponse processRefund(Long returnId) {
        ReturnRequest entity = findEntity(returnId);
        if (entity.getReturnStatus() != ReturnStatus.APPROVED) {
            throw new IllegalStateException(
                    "Only APPROVED returns can be refunded. Current state: "
                            + entity.getReturnStatus());
        }
        entity.setReturnStatus(ReturnStatus.REFUNDED);
        if (entity.getOrder() != null && entity.getOrder().getCustomer() != null) {
            Long customerId = entity.getOrder().getCustomer().getCustomerId();
            double refundValue = entity.getRefundAmount() != null ? entity.getRefundAmount().doubleValue() : 0.0;

            // Execute down the point rollback pipeline
            customerService.deductPointsFromRefund(customerId, refundValue);
        }

        return ReturnMapper.toResponse(returnRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ReturnResponse getReturnRequest(Long returnId) {
        return ReturnMapper.toResponse(findEntity(returnId));
    }


    @Transactional(readOnly = true)
    public List<ReturnResponse> getAllReturns() {
        return returnRepository.findAll().stream()
                .map(ReturnMapper::toResponse)
                .toList();
    }

    private ReturnRequest findEntity(Long returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Return request not found with id: " + returnId));
    }

    private boolean isWithinReturnWindow(LocalDate requestDate) {
        LocalDate earliestAllowed = LocalDate.now().minusDays(RETURN_WINDOW_DAYS);
        return !requestDate.isBefore(earliestAllowed) && !requestDate.isAfter(LocalDate.now());
    }
}
