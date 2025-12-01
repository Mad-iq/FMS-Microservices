package com.flight.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchFlightRequest {

    @NotBlank
    private String source;

    @NotBlank
    private String destination;

    @NotBlank
    private String journeyDate;

    @Min(1)
    private int numberOfPassengers;

    private boolean roundTrip;
    private String returnDate;
}
