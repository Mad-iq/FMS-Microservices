package com.flight.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flight.model.City;
import com.flight.model.Flight;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Flight findByFlightId(String flightId);

    List<Flight> findBySourceAndDestinationAndStartDateBetween(
            City source,
            City destination,
            LocalDateTime start,
            LocalDateTime end
    );
}
