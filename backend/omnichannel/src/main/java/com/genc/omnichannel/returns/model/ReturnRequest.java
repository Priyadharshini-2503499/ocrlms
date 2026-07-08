package com.genc.omnichannel.returns.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.genc.omnichannel.order.model.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "returnrequest")
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "returnId")
    private Long returnId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private Order order;

    @Column(name = "returnReason", length = 255)
    private String returnReason;

    @Column(name = "refundAmount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "requestDate", nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "returnStatus", nullable = false, length = 20)
    private ReturnStatus returnStatus = ReturnStatus.REQUESTED;

    public ReturnRequest() {
    }

    public ReturnRequest(Order order, String returnReason, BigDecimal refundAmount,
                          LocalDate requestDate, ReturnStatus returnStatus) {
        this.order = order;
        this.returnReason = returnReason;
        this.refundAmount = refundAmount;
        this.requestDate = requestDate;
        this.returnStatus = returnStatus;
    }

    public Long getReturnId() {
        return returnId;
    }

    public void setReturnId(Long returnId) {
        this.returnId = returnId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public ReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(ReturnStatus returnStatus) {
        this.returnStatus = returnStatus;
    }

    @Override
    public String toString() {
        return "ReturnRequest{" +
                "returnId=" + returnId +
                ", orderId=" + (order != null ? order.getOrderId() : null) +
                ", returnReason='" + returnReason + '\'' +
                ", refundAmount=" + refundAmount +
                ", requestDate=" + requestDate +
                ", returnStatus=" + returnStatus +
                '}';
    }
}
