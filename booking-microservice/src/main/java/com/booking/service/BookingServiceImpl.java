package com.booking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booking.feign.FeignInterface;
import com.booking.model.Booking;
import com.booking.model.BookingStatus;
import com.booking.model.Passenger;
import com.booking.publisher.EmailPublisher;
import com.booking.repositories.BookingRepository;
import com.booking.request.BookingRequest;
import com.booking.request.PassengerRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepo;
    private final FeignInterface flightClient;
    private final EmailPublisher emailPublisher;


    //circuitbreaker stuff

    @CircuitBreaker(name = "flightServiceCB", fallbackMethod = "flightInfoFallback")
    public Map<String, Object> safeGetFlightInfo(String flightId) {
        try {
            return flightClient.getFlightInfo(flightId);
        } catch (Throwable ex) {
            System.out.println("safeGetFlightInfo: exception -> " + ex.getMessage());
            return flightInfoFallback(flightId, ex);
        }
    }

    @CircuitBreaker(name = "flightServiceCB", fallbackMethod = "reserveFallback")
    public Map<String, Object> safeReserveSeats(String flightId, Map<String, Object> body) {
        try {
            return flightClient.reserveSeats(flightId, body);
        } catch (Throwable ex) {
            System.out.println("safeReserveSeats: exception -> " + ex.getMessage());
            return reserveFallback(flightId, body, ex);
        }
    }

    @CircuitBreaker(name = "flightServiceCB", fallbackMethod = "releaseFallback")
    public Map<String, Object> safeReleaseSeats(String flightId, Map<String, Object> body) {
        try {
            return flightClient.releaseSeats(flightId, body);
        } catch (Throwable ex) {
            System.out.println("safeReleaseSeats: exception -> " + ex.getMessage());
            return releaseFallback(flightId, body, ex);
        }
    }

    //fallback stuff

    public Map<String, Object> flightInfoFallback(String flightId, Throwable ex) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("fallback", true);
        resp.put("status", "UNAVAILABLE");
        resp.put("message", "Flight Service is down. Please try again later.");
        resp.put("availableSeatNumbers", List.<String>of()); // empty list
        resp.put("price", 0);
        resp.put("startDate", null);
        resp.put("error", ex == null ? null : ex.getMessage());
        return resp;
    }

    public Map<String, Object> reserveFallback(String flightId, Map<String, Object> body, Throwable ex) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("fallback", true);
        resp.put("message", "Seat reservation is temporarily unavailable.");
        resp.put("error", ex == null ? null : ex.getMessage());
        return resp;
    }

    public Map<String, Object> releaseFallback(String flightId, Map<String, Object> body, Throwable ex) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("fallback", true);
        resp.put("message", "Seat release pending: Flight Service unavailable.");
        resp.put("error", ex == null ? null : ex.getMessage());
        return resp;
    }

//main methods

    @Override
    @Transactional
    public Map<String, Object> bookTicket(String flightId, BookingRequest req) {

        if (req.getPassengers() == null || req.getPassengers().size() != req.getNumberOfSeats()) {
            throw new RuntimeException("numberOfSeats must equal number of passengers");
        }

        if (req.getSeatNumbers() == null || req.getSeatNumbers().size() != req.getNumberOfSeats()) {
            throw new RuntimeException("Seat numbers count must match number of seats");
        }

        Map<String, Object> flightInfo = safeGetFlightInfo(flightId);

        if (Boolean.TRUE.equals(flightInfo.get("fallback")) || "UNAVAILABLE".equals(flightInfo.get("status"))) {
            return Map.of(
                    "status", "FAILED",
                    "message", "Cannot book right now: Flight service is unavailable"
            );
        }

        @SuppressWarnings("unchecked")
        List<String> availableSeats = (List<String>) flightInfo.getOrDefault("availableSeatNumbers", List.of());
        if (!availableSeats.containsAll(req.getSeatNumbers())) {
            return Map.of("status", "FAILED", "message", "Requested seats are unavailable");
        }

        Map<String, Object> reserveBody = Map.of("seatNumbers", req.getSeatNumbers());
        Map<String, Object> reserveResult = safeReserveSeats(flightId, reserveBody);

        if (Boolean.TRUE.equals(reserveResult.get("fallback"))) {
            return Map.of("status", "FAILED", "message", "Seat reservation failed (Flight service unavailable)");
        }

        if (!"Reserved".equalsIgnoreCase(String.valueOf(reserveResult.getOrDefault("message", "")))) {
            return Map.of("status", "FAILED", "message", "Failed to reserve seats");
        }

        Booking booking = new Booking();
        booking.setPnr(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setEmail(req.getEmail());
        booking.setName(req.getName());
        booking.setTimeOfBooking(LocalDateTime.now());
        booking.setFlightId(flightId);
        booking.setStatus(BookingStatus.CONFIRMED);

        if (flightInfo.get("startDate") != null) {
            try {
                booking.setTimeOfJourney(LocalDateTime.parse(String.valueOf(flightInfo.get("startDate"))));
            } catch (Exception e) {
            }
        }

        booking.setNumberOfSeats(req.getNumberOfSeats());
        double price = 0.0;
        Object priceObj = flightInfo.get("price");
        if (priceObj instanceof Number) {
            price = ((Number) priceObj).doubleValue();
        } else if (priceObj != null) {
            try {
                price = Double.parseDouble(String.valueOf(priceObj));
            } catch (Exception ignore) {}
        }
        booking.setTotalPrice(price * req.getNumberOfSeats());

        List<Passenger> passengerList = new ArrayList<>();
        for (PassengerRequest pr : req.getPassengers()) {
            Passenger p = new Passenger();
            p.setName(pr.getName());
            p.setGender(pr.getGender());
            p.setAge(pr.getAge());
            passengerList.add(p);
        }
        booking.setPassengers(passengerList);
        booking.setSeatNumbers(req.getSeatNumbers());

        bookingRepo.save(booking);
        try {
            com.booking.events.EmailPayload payload = new com.booking.events.EmailPayload(
                booking.getEmail(),
                booking.getName(),
                booking.getPnr(),
                booking.getFlightId()
            );
            emailPublisher.publishEmailEvent(payload);
        } catch (Exception e) {
            System.err.println("Failed to publish email event: " + e.getMessage());
        }


        return Map.of(
                "pnr", booking.getPnr(),
                "message", "Booking successful",
                "totalPrice", booking.getTotalPrice()
        );
    }

    @Override
    public Map<String, Object> getTicketByPnr(String pnr) {
        Booking b = bookingRepo.findByPnr(pnr);
        if (b == null) throw new RuntimeException("Booking not found");

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("pnr", b.getPnr());
        resp.put("flightId", b.getFlightId());
        resp.put("totalPrice", b.getTotalPrice());
        resp.put("status", b.getStatus().name());
        resp.put("seatNumbers", b.getSeatNumbers());
        resp.put("passengers", b.getPassengers());
        return resp;
    }

    @Override
    public Map<String, Object> getBookingHistory(String email) {
        List<Booking> list = bookingRepo.findByEmail(email);

        List<Map<String, Object>> history = new ArrayList<>();
        for (Booking b : list) {
            history.add(Map.of(
                    "pnr", b.getPnr(),
                    "flightId", b.getFlightId(),
                    "status", b.getStatus().name()
            ));
        }
        return Map.of("email", email, "history", history);
    }

    @Override
    @Transactional
    public Map<String, Object> cancelBooking(String pnr) {
        Booking b = bookingRepo.findByPnr(pnr);
        if (b == null) throw new RuntimeException("Booking not found");
        
        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime journeyTime = b.getTimeOfJourney();
        
        if (now.isAfter(journeyTime)) {
            throw new RuntimeException("Journey has already started. Cancellation not allowed.");
        }
        
        long hoursLeft = Duration.between(now, journeyTime).toHours();
        if (hoursLeft < 24) {
            throw new RuntimeException(
                "Cancellation not allowed within 24 hours of journey time"
            );
        }

        b.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(b);

        Map<String, Object> body = Map.of("seatNumbers", b.getSeatNumbers());
        Map<String, Object> releaseResp = safeReleaseSeats(b.getFlightId(), body);
        if (Boolean.TRUE.equals(releaseResp.get("fallback"))) {
            System.out.println("releaseFallback triggered while cancelling: " + releaseResp.get("message"));
        }

        return Map.of("pnr", pnr, "message", "Ticket cancelled successfully");
    }
    
    @Override
    public Map<String, Object> getBookingHistoryWithDetails(String email) {
        List<Booking> bookings = bookingRepo.findByEmail(email);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Booking b : bookings) {
            Map<String, Object> flightInfo = safeGetFlightInfo(b.getFlightId());
            Map<String, Object> bookingMap = new LinkedHashMap<>();
            bookingMap.put("pnr", b.getPnr());
            bookingMap.put("numberOfPassengers", b.getPassengers().size());
            bookingMap.put("totalPrice", b.getTotalPrice());
            bookingMap.put("journeyDate", b.getTimeOfJourney());
            bookingMap.put("status", b.getStatus().name());
            result.add(bookingMap);
        }

        return Map.of(
            "email", email,
            "bookings", result
        );
    }


}
