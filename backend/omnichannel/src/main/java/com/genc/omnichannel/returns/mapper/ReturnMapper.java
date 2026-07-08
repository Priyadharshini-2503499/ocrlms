package com.genc.omnichannel.returns.mapper;

import com.genc.omnichannel.returns.dto.InitiateReturnRequest;
import com.genc.omnichannel.returns.dto.ReturnResponse;
import com.genc.omnichannel.returns.model.ReturnRequest;


public final class ReturnMapper {

    private ReturnMapper() {
       
    }

    public static ReturnRequest toEntity(InitiateReturnRequest request) {
        ReturnRequest entity = new ReturnRequest();
        entity.setReturnReason(request.getReturnReason());
        entity.setRequestDate(request.getRequestDate());
        return entity;
    }

 
    public static ReturnResponse toResponse(ReturnRequest entity) {
        return new ReturnResponse(
                entity.getReturnId(),
                entity.getOrder() != null ? entity.getOrder().getOrderId() : null,
                entity.getReturnReason(),
                entity.getRefundAmount(),
                entity.getRequestDate(),
                entity.getReturnStatus());
    }
}
