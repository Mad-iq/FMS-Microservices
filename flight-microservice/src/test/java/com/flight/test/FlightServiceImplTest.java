package com.flight.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flight.model.Airline;
import com.flight.model.City;
import com.flight.model.Flight;
import com.flight.repositories.AirlineRepository;
import com.flight.repositories.FlightRepository;
import com.flight.request.AddFlightRequest;
import com.flight.request.SearchFlightRequest;
import com.flight.service.FlightServiceImpl;

public class FlightServiceImplTest {

    @Mock
    private AirlineRepository airlineRepo;

    @Mock
    private FlightRepository flightRepo;

    @InjectMocks
    private FlightServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void addFlight_createsNewAirlineAnd_generatesSeats() {
        AddFlightRequest req = new AddFlightRequest();
        req.setAirlineName("Indigo");
        req.setSource("DELHI");
        req.setDestination("MUMBAI");
        req.setStartDate("2025-01-01T10:00:00");
        req.setEndDate("2025-01-01T12:00:00");
        req.setAvailableSeats(3);
        req.setTicketPrice(2500);
        req.setMealStatus(true);

        when(airlineRepo.findByName("Indigo")).thenReturn(null);

        Airline savedAirline = new Airline();
        savedAirline.setName("Indigo");
        when(airlineRepo.save(any())).thenReturn(savedAirline);

        Map<String, Object> resp = service.addFlight(req);

        assertTrue(resp.containsKey("flightId"));
        verify(flightRepo, times(1)).save(any(Flight.class));
    }


    @Test
    public void addFlight_reusesExistingAirline() {
        Airline airline = new Airline();
        airline.setName("Vistara");

        AddFlightRequest req = new AddFlightRequest();
        req.setAirlineName("Vistara");
        req.setSource("DELHI");
        req.setDestination("KOLKATA");
        req.setStartDate("2025-01-02T10:00:00");
        req.setEndDate("2025-01-02T12:00:00");
        req.setAvailableSeats(2);
        req.setTicketPrice(2000);
        req.setMealStatus(false);

        when(airlineRepo.findByName("Vistara")).thenReturn(airline);

        Map<String, Object> resp = service.addFlight(req);

        assertTrue(resp.containsKey("flightId"));
        verify(flightRepo).save(any(Flight.class));
    }


    @Test
    public void searchFlights_onlyOnward_whenNotRoundTrip() {
        SearchFlightRequest req = new SearchFlightRequest();
        req.setSource("DELHI");
        req.setDestination("KOLKATA");
        req.setJourneyDate("2025-01-10");
        req.setRoundTrip(false);

        Flight f1 = new Flight();
        f1.setFlightId("F1");
        f1.setStartDate(LocalDateTime.parse("2025-01-10T10:00:00"));
        f1.setTicketPrice(3000);
        f1.setAvailableSeats(50);
        Airline air = new Airline();
        air.setName("Indigo");
        f1.setAirline(air);

        when(flightRepo.findBySourceAndDestinationAndStartDateBetween(
                eq(City.DELHI), eq(City.KOLKATA),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(f1));

        Map<String, Object> resp = service.searchFlights(req);

        assertTrue(resp.containsKey("onwardFlights"));
        assertEquals(1, ((List<?>) resp.get("onwardFlights")).size());
    }


    @Test
    public void searchFlights_returnsOnwardAndReturn_whenRoundTrip() {
        SearchFlightRequest req = new SearchFlightRequest();
        req.setSource("DELHI");
        req.setDestination("KOLKATA");
        req.setJourneyDate("2025-01-10");
        req.setRoundTrip(true);
        req.setReturnDate("2025-01-15");

        Flight onward = new Flight();
        onward.setFlightId("ON1");
        onward.setStartDate(LocalDateTime.parse("2025-01-10T10:00:00"));
        Airline a1 = new Airline();
        a1.setName("AirTest");
        onward.setAirline(a1);

        Flight ret = new Flight();
        ret.setFlightId("RT1");
        ret.setStartDate(LocalDateTime.parse("2025-01-15T12:00:00"));
        Airline a2 = new Airline();
        a2.setName("AirTestReturn");
        ret.setAirline(a2);

        when(flightRepo.findBySourceAndDestinationAndStartDateBetween(
                eq(City.DELHI), eq(City.KOLKATA),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(onward));

        when(flightRepo.findBySourceAndDestinationAndStartDateBetween(
                eq(City.KOLKATA), eq(City.DELHI),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(ret));

        Map<String, Object> resp = service.searchFlights(req);

        assertTrue(resp.containsKey("onwardFlights"));
        assertTrue(resp.containsKey("returnFlights"));
    }


    @Test
    public void reserveSeats_success_and_failure() {
        Flight f = new Flight();
        f.setFlightId("F1");
        f.setAvailableSeatNumbers(new LinkedHashSet<>(Set.of("1A", "1B")));
        f.setAvailableSeats(2);

        when(flightRepo.findByFlightId("F1")).thenReturn(f);

        boolean ok = service.reserveSeats("F1", List.of("1A"));
        assertTrue(ok);
        assertEquals(Set.of("1B"), f.getAvailableSeatNumbers());

        boolean fail = service.reserveSeats("F1", List.of("XX"));
        assertFalse(fail);
    }

    @Test
    public void releaseSeats_addsBackSeats() {
        Flight f = new Flight();
        f.setFlightId("F1");
        f.setAvailableSeatNumbers(new LinkedHashSet<>(Set.of("1A")));
        f.setAvailableSeats(1);

        when(flightRepo.findByFlightId("F1")).thenReturn(f);

        boolean ok = service.releaseSeats("F1", List.of("1B"));
        assertTrue(ok);
        assertEquals(Set.of("1A", "1B"), f.getAvailableSeatNumbers());
    }

    @Test
    public void getFlightInfo_found_and_notFound() {
        Flight f = new Flight();
        f.setFlightId("G1");

        Airline airline = new Airline();
        airline.setName("TestAir");
        f.setAirline(airline);

        f.setStartDate(LocalDateTime.parse("2025-01-01T10:00:00"));
        f.setTicketPrice(123.45);

        Set<String> seats = new LinkedHashSet<>(Set.of("1A"));
        f.setAvailableSeatNumbers(seats);
        f.setAvailableSeats(1);

        when(flightRepo.findByFlightId("G1")).thenReturn(f);

        Map<String, Object> info = service.getFlightInfo("G1");
        assertEquals("G1", info.get("flightId"));
        assertEquals("TestAir", info.get("airline"));
        assertEquals(1, ((Set<?>) info.get("availableSeatNumbers")).size());

        when(flightRepo.findByFlightId("NOT")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.getFlightInfo("NOT")
        );
        assertTrue(ex.getMessage().contains("Flight not found"));
    }
}