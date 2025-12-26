package com.flight.service;

import java.util.List;
import java.util.Map;

import com.flight.model.Flight;
import com.flight.request.AddFlightRequest;
import com.flight.request.SearchFlightRequest;

public interface FlightService {
    Map<String, Object> addFlight(AddFlightRequest req);
    Map<String, Object> searchFlights(SearchFlightRequest req);
    boolean reserveSeats(String flightId, List<String> seatNumbers);
    boolean releaseSeats(String flightId, List<String> seatNumbers);
    Map<String, Object> getFlightInfo(String flightId);
    List<Map<String, Object>> addMultipleFlights(List<AddFlightRequest> requests);
    List<Map<String, Object>> getAllFlights();
}
