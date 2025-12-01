package com.flight.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.flight.model.Airline;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
    Airline findByName(String name);
}
