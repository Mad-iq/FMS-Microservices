package com.flight.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.flight.model.Airline;
import com.flight.model.City;
import com.flight.model.Flight;
import com.flight.model.MealStatus;
import com.flight.repositories.AirlineRepository;
import com.flight.repositories.FlightRepository;
import com.flight.request.AddFlightRequest;
import com.flight.request.SearchFlightRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final AirlineRepository airlineRepo;
    private final FlightRepository flightRepo;

    @Override
    @Transactional
    public Map<String, Object> addFlight(AddFlightRequest req) {

        Airline airline = airlineRepo.findByName(req.getAirlineName());
        if (airline == null) {
            airline = new Airline();
            airline.setName(req.getAirlineName());
            airlineRepo.save(airline);
        }

        LocalDateTime start = LocalDateTime.parse(req.getStartDate());
        LocalDateTime end = LocalDateTime.parse(req.getEndDate());

        String flightId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Flight flight = new Flight();
        flight.setFlightId(flightId);
        flight.setSource(City.valueOf(req.getSource().toUpperCase()));
        flight.setDestination(City.valueOf(req.getDestination().toUpperCase()));
        flight.setStartDate(start);
        flight.setEndDate(end);
        flight.setAvailableSeats(req.getAvailableSeats());
        flight.setTicketPrice(req.getTicketPrice());
        flight.setMealStatus(req.isMealStatus() ? MealStatus.VEG : MealStatus.NONVEG);
        flight.setAirline(airline);

        Set<String> seats = new LinkedHashSet<>();
        int total = req.getAvailableSeats();
        int row = 1;
        char letter = 'A';

        for (int i = 0; i < total; i++) {
            seats.add(row + "" + letter);
            letter++;
            if (letter > 'F') { letter = 'A'; row++; }
        }

        flight.setAvailableSeatNumbers(seats);

        flightRepo.save(flight);

        return Map.of(
                "message", "Flight added successfully",
                "flightId", flightId
        );
    }
    
    @Override
    public List<Map<String, Object>> addMultipleFlights(List<AddFlightRequest> requests) {

        List<Map<String, Object>> responses = new ArrayList<>();
        for (AddFlightRequest req : requests) {
            Map<String, Object> result = addFlight(req);
            responses.add(result);
        }

        return responses;
    }



    @Override
    public Map<String, Object> searchFlights(SearchFlightRequest req) {

        City src = City.valueOf(req.getSource().toUpperCase());
        City dst = City.valueOf(req.getDestination().toUpperCase());
        LocalDate journey = LocalDate.parse(req.getJourneyDate());

        List<Flight> onward = flightRepo.findBySourceAndDestinationAndStartDateBetween(
                src, dst, journey.atStartOfDay(), journey.atTime(23, 59)
        );

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("onwardFlights", mapFlights(onward));

        if (req.isRoundTrip()) {
            LocalDate ret = LocalDate.parse(req.getReturnDate());
            List<Flight> retFlights = flightRepo.findBySourceAndDestinationAndStartDateBetween(
                    dst, src, ret.atStartOfDay(), ret.atTime(23, 59)
            );
            resp.put("returnFlights", mapFlights(retFlights));
        }

        return resp;
    }

    private List<Map<String, Object>> mapFlights(List<Flight> list) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Flight f : list) {
            out.add(Map.of(
                    "flightId", f.getFlightId(),
                    "airline", f.getAirline().getName(),
                    "dateTime", f.getStartDate().toString(),
                    "price", f.getTicketPrice(),
                    "availableSeats", f.getAvailableSeats(),
                    "availableSeatNumbers", f.getAvailableSeatNumbers()
            ));
        }
        return out;
    }

    @Override
    @Transactional
    public boolean reserveSeats(String flightId, List<String> seatNumbers) {
        Flight f = flightRepo.findByFlightId(flightId);
        if (f == null) return false;

        if (!f.getAvailableSeatNumbers().containsAll(seatNumbers))
            return false;

        f.getAvailableSeatNumbers().removeAll(seatNumbers);
        f.setAvailableSeats(f.getAvailableSeatNumbers().size());
        flightRepo.save(f);

        return true;
    }

    @Override
    @Transactional
    public boolean releaseSeats(String flightId, List<String> seatNumbers) {
        Flight f = flightRepo.findByFlightId(flightId);
        if (f == null) return false;

        f.getAvailableSeatNumbers().addAll(seatNumbers);
        f.setAvailableSeats(f.getAvailableSeatNumbers().size());
        flightRepo.save(f);

        return true;
    }

    @Override
    public Map<String, Object> getFlightInfo(String flightId) {
        Flight f = flightRepo.findByFlightId(flightId);
        if (f == null) throw new RuntimeException("Flight not found");
        return Map.of(
                "flightId", f.getFlightId(),
                "airline", f.getAirline().getName(),
                "startDate", f.getStartDate().toString(),
                "price", f.getTicketPrice(),
                "availableSeats", f.getAvailableSeats(),
                "availableSeatNumbers", f.getAvailableSeatNumbers()
        );
    }
}
