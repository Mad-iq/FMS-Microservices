package com.flight.test;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.controller.FlightController;
import com.flight.request.AddFlightRequest;
import com.flight.request.SearchFlightRequest;
import com.flight.service.FlightService;

public class FlightControllerTest {

    @Mock
    private FlightService service;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        FlightController controller = new FlightController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void addFlight_endpoint_returns201() throws Exception {
        AddFlightRequest req = new AddFlightRequest();
        req.setAirlineName("AirX");
        req.setSource("KOL");
        req.setDestination("DEL");
        req.setStartDate(LocalDateTime.now().plusDays(1).toString());
        req.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2).toString());
        req.setAvailableSeats(6);
        req.setTicketPrice(100.0);
        req.setMealStatus(true);

        when(service.addFlight(any())).thenReturn(Map.of("message","Flight added successfully","flightId","ABC"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/flight")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Flight added successfully"))
                .andExpect(jsonPath("$.flightId").value("ABC"));
    }


    @Test
    public void reserve_and_release_endpoints_return_expected_codes() throws Exception {
        when(service.reserveSeats(eq("F1"), anyList())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/flight/inventory/reserve/F1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatNumbers", List.of("1A", "1B")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserved"));


        when(service.reserveSeats(eq("F2"), anyList())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/flight/inventory/reserve/F2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatNumbers", List.of("99Z")))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Seats not available"));

        when(service.releaseSeats(eq("F1"), anyList())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/flight/inventory/release/F1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatNumbers", List.of("1A")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Released"));

 
        when(service.releaseSeats(eq("F3"), anyList())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/flight/inventory/release/F3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatNumbers", List.of("1X")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Release failed"));
    }

    @Test
    public void getInfo_endpoint_returns_ok() throws Exception {
        when(service.getFlightInfo("F1")).thenReturn(Map.of("flightId","F1","price",100.0));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/flight/inventory/F1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightId").value("F1"))
                .andExpect(jsonPath("$.price").value(100.0));
    }
}