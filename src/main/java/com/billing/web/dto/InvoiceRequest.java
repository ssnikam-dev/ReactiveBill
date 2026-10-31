package com.billing.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Payload used to create or update a DRAFT invoice")
public record InvoiceRequest(
        @Schema(description = "Customer billed for this invoice", example = "Acme Corp", maxLength = 100)
        @NotBlank @Size(max = 100)
        String customerName,

        @Schema(description = "Invoice total (min 0.01, up to 2 decimal places)", example = "1500.00")
        @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2)
        BigDecimal amount,

        @Schema(description = "ISO currency code", example = "INR", allowableValues = {"INR", "USD", "EUR"})
        @NotBlank @Pattern(regexp = "INR|USD|EUR", message = "must be one of INR, USD, EUR")
        String currency) {}
