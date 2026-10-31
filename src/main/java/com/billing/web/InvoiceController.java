package com.billing.web;

import com.billing.domain.InvoiceStatus;
import com.billing.service.InvoiceService;
import com.billing.web.dto.InvoiceRequest;
import com.billing.web.dto.InvoiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Create, query and move invoices through their lifecycle")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @Operation(summary = "Create an invoice (starts as DRAFT)")
    @ApiResponse(responseCode = "201", description = "Created; Location header points to the new invoice", content = @Content(schema = @Schema(implementation = InvoiceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public Mono<ResponseEntity<InvoiceResponse>> create(@Valid @RequestBody InvoiceRequest request) {
        return service.create(request)
                .map(InvoiceResponse::from)
                .map(body -> ResponseEntity.created(URI.create("/api/invoices/" + body.id())).body(body));
    }

    @Operation(summary = "List invoices as a reactive stream, optionally filtered")
    @ApiResponse(responseCode = "200", description = "Matching invoices")
    @GetMapping
    public Flux<InvoiceResponse> list(@Parameter(description = "Only invoices with this status") @RequestParam(required = false) InvoiceStatus status,
                                      @Parameter(description = "Customer name filter", example = "acme") @RequestParam(required = false) String customer) {
        return service.list(status, customer).map(InvoiceResponse::from);
    }

    @Operation(summary = "Total amount per invoice status")
    @ApiResponse(responseCode = "200", description = "Map of status to total amount")
    @GetMapping("/summary")
    public Mono<Map<InvoiceStatus, BigDecimal>> summary() {
        return service.totalsByStatus();
    }

    @Operation(summary = "Get one invoice")
    @ApiResponse(responseCode = "200", description = "The invoice")
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public Mono<InvoiceResponse> get(@Parameter(description = "Invoice id", example = "1") @PathVariable Long id) {
        return service.get(id).map(InvoiceResponse::from);
    }

    @Operation(summary = "Update an invoice (DRAFT only)")
    @ApiResponse(responseCode = "200", description = "Updated invoice")
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Invoice is no longer DRAFT", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{id}")
    public Mono<InvoiceResponse> update(@Parameter(description = "Invoice id", example = "1") @PathVariable Long id, @Valid @RequestBody InvoiceRequest request) {
        return service.update(id, request).map(InvoiceResponse::from);
    }

    @Operation(summary = "Mark a DRAFT invoice as PAID")
    @ApiResponse(responseCode = "200", description = "Invoice is now PAID")
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Invoice is no longer DRAFT", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{id}/pay")
    public Mono<InvoiceResponse> pay(@Parameter(description = "Invoice id", example = "1") @PathVariable Long id) {
        return service.markPaid(id).map(InvoiceResponse::from);
    }

    @Operation(summary = "Mark a DRAFT invoice as CANCELLED")
    @ApiResponse(responseCode = "200", description = "Invoice is now CANCELLED")
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Invoice is no longer DRAFT", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{id}/cancel")
    public Mono<InvoiceResponse> cancel(@Parameter(description = "Invoice id", example = "1") @PathVariable Long id) {
        return service.cancel(id).map(InvoiceResponse::from);
    }

    @Operation(summary = "Delete an invoice (DRAFT only)")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Invoice is no longer DRAFT", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@Parameter(description = "Invoice id", example = "1") @PathVariable Long id) {
        return service.delete(id);
    }
}
