package com.flight.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.flight.model.Airline;

public class AirlineModelTest {

    @Test
    public void lombok_getters_setters() {
        Airline a = new Airline();
        a.setName("TestAir");
        assertEquals("TestAir", a.getName());
        a.setName("NewName");
        assertEquals("NewName", a.getName());
    }
}
