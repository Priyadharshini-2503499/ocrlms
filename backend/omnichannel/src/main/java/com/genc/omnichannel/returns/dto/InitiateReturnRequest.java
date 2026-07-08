package com.genc.omnichannel.returns.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class InitiateReturnRequest {

    private Long orderId;
    private String returnReason;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate requestDate;

    public InitiateReturnRequest() {
    }

    public Long getOrderId() {

        return orderId;
    }

    public void setOrderId(Long orderId) {

        this.orderId = orderId;
    }

    public String getReturnReason() {

        return returnReason;
    }

    public void setReturnReason(String returnReason) {

        this.returnReason = returnReason;
    }


    public LocalDate getRequestDate() {

        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }
}
