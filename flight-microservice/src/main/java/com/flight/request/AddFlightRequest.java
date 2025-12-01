package com.flight.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddFlightRequest {

    @NotBlank
    private String airlineName;

    @NotBlank
    private String source;

    @NotBlank
    private String destination;

    @NotBlank
    private String startDate;

    @NotBlank
    private String endDate;

    @Min(1)
    private int availableSeats;

    @Min(1)
    private double ticketPrice;

    private boolean mealStatus;
}
