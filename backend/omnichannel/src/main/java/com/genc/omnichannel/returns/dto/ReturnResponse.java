package com.genc.omnichannel.returns.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.genc.omnichannel.returns.model.ReturnStatus;


public class ReturnResponse {

    private final Long returnId;
    private final Long orderId;
    private final String returnReason;
    private final BigDecimal refundAmount;
    private final LocalDate requestDate;
    private final ReturnStatus returnStatus;

    public ReturnResponse(Long returnId, Long orderId, String returnReason,
                          BigDecimal refundAmount, LocalDate requestDate, ReturnStatus returnStatus) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.returnReason = returnReason;
        this.refundAmount = refundAmount;
        this.requestDate = requestDate;
        this.returnStatus = returnStatus;
    }

    public Long getReturnId() {
        return returnId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public ReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public String getBadgeClass() {
        if (returnStatus == null) {
            return "b-gray";
        }
        return switch (returnStatus) {
            case REQUESTED -> "b-gold";
            case APPROVED, REFUNDED -> "b-green";
            case REJECTED -> "b-rust";
        };
    }

    public boolean isApprovable() {
        return returnStatus == ReturnStatus.REQUESTED;
    }

    public boolean isRefundable() {
        return returnStatus == ReturnStatus.APPROVED;
    }
}
