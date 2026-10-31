package com.billing.web.dto;

import com.billing.domain.Invoice;
import io.swagger.v3.oas.annotations.media.Schema;
import com.billing.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "An invoice as stored in the database")
public record InvoiceResponse(
        @Schema(example = "1") Long id,
        String invoiceNumber,
        String customerName,
        BigDecimal amount,
        String currency,
        @Schema(description = "DRAFT can be edited, paid, cancelled or deleted. PAID and CANCELLED are final.", example = "DRAFT")
        InvoiceStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static InvoiceResponse from(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getInvoiceNumber(), i.getCustomerName(), i.getAmount(),
                i.getCurrency(), i.getStatus(), i.getCreatedAt(), i.getUpdatedAt());
    }
}
