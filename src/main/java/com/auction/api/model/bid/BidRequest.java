package com.auction.api.model.bid;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Bid request model")
public class BidRequest {

    @NotNull(message = "Bid amount is mandatory.")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than zero.")
    @Schema(description = "Amount of the bid", example = "250.00")
    private Double amount;
}
