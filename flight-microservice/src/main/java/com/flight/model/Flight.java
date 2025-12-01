package com.flight.model;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String flightId; 

    @Enumerated(EnumType.STRING)
    private City source;

    @Enumerated(EnumType.STRING)
    private City destination;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private int availableSeats;
    private double ticketPrice;

    @Enumerated(EnumType.STRING)
    private MealStatus mealStatus;

    @ManyToOne
    private Airline airline;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "flight_available_seats", joinColumns = @JoinColumn(name = "flight_id"))
    @Column(name = "seat_number")
    private Set<String> availableSeatNumbers = new LinkedHashSet<>();
}
