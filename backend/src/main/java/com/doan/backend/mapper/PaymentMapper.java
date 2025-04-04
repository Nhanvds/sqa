package com.doan.backend.mapper;

import com.doan.backend.dto.response.PaymentResponse;
import com.doan.backend.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toPaymentResponse(Payment payment);
}
