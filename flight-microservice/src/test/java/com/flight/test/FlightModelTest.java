package com.flight.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.flight.model.Flight;

public class FlightModelTest {

    @Test
    public void lombok_getters_setters_and_collections_work() {
        Flight f = new Flight();
        f.setFlightId("ABC123");
        f.setAvailableSeats(6);
        f.setTicketPrice(199.99);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        f.setStartDate(start);
        f.setEndDate(end);
        Set<String> seats = new LinkedHashSet<>();
        seats.add("1A");
        seats.add("1B");
        f.setAvailableSeatNumbers(seats);

        assertEquals("ABC123", f.getFlightId());
        assertEquals(6, f.getAvailableSeats());
        assertEquals(199.99, f.getTicketPrice());
        assertEquals(start, f.getStartDate());
        assertEquals(end, f.getEndDate());
        assertTrue(f.getAvailableSeatNumbers().contains("1A"));
        assertTrue(f.getAvailableSeatNumbers().contains("1B"));

        f.getAvailableSeatNumbers().remove("1A");
        assertFalse(f.getAvailableSeatNumbers().contains("1A"));
    }
}

