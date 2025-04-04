package com.doan.backend.mapper;

import com.doan.backend.dto.response.InvoiceResponse;
import com.doan.backend.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderMapper.class, PaymentMapper.class})
public interface InvoiceMapper {

    @Mapping(target = "payment", source = "payment")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    @Mapping(target = "payment", source = "payment")
    Iterable<InvoiceResponse> toInvoiceResponseIterable(Iterable<Invoice> invoices);
}
