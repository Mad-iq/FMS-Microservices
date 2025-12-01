package com.flight.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import com.flight.exception.ApiError;
import com.flight.exception.GlobalExceptionHandler;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        webRequest = Mockito.mock(WebRequest.class);
        Mockito.when(webRequest.getDescription(false)).thenReturn("uri=/test/url");
    }



    @Test
    void testHandleRuntime_FlightNotFound() {
        RuntimeException ex = new RuntimeException("Flight not found");
        ResponseEntity<ApiError> response = handler.handleRuntime(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Flight not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

  
    @Test
    void testHandleRuntime_OtherError() {
        RuntimeException ex = new RuntimeException("Invalid seat");
        ResponseEntity<ApiError> response = handler.handleRuntime(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid seat", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }


    @Test
    void testHandleGeneric() {
        Exception ex = new Exception("Server blew up");
        ResponseEntity<ApiError> response = handler.handleGeneric(ex, webRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Server blew up", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
    }
}

