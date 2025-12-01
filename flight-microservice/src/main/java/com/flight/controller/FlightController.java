
package com.flight.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flight.request.AddFlightRequest;
import com.flight.request.SearchFlightRequest;
import com.flight.service.FlightService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService service;

    @PostMapping
    public ResponseEntity<?> addFlight(
            @Valid @RequestBody AddFlightRequest req
    ) {
        return ResponseEntity.status(201).body(service.addFlight(req));
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@Valid @RequestBody SearchFlightRequest req) {
        return ResponseEntity.ok(service.searchFlights(req));
    }

    @PostMapping("/inventory/reserve/{flightId}")
    public ResponseEntity<?> reserve(@PathVariable String flightId,
                                     @RequestBody Map<String, Object> body) {
        List<String> seatNumbers = (List<String>) body.get("seatNumbers");

        boolean ok = service.reserveSeats(flightId, seatNumbers);
        if (ok) return ResponseEntity.ok(Map.of("message", "Reserved"));

        return ResponseEntity.status(409).body(Map.of("message", "Seats not available"));
    }

    @PostMapping("/inventory/release/{flightId}")
    public ResponseEntity<?> release(@PathVariable String flightId,
                                     @RequestBody Map<String, Object> body) {
        List<String> seatNumbers = (List<String>) body.get("seatNumbers");

        boolean ok = service.releaseSeats(flightId, seatNumbers);
        if (ok) return ResponseEntity.ok(Map.of("message", "Released"));

        return ResponseEntity.badRequest().body(Map.of("message", "Release failed"));
    }

    @GetMapping("/inventory/{flightId}")
    public ResponseEntity<?> getInfo(@PathVariable String flightId) {
        return ResponseEntity.ok(service.getFlightInfo(flightId));
    }
}

