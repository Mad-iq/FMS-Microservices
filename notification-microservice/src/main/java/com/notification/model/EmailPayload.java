package com.notification.model;

import lombok.Data;

@Data
public class EmailPayload {
    private String email;
    private String name;
    private String pnr;
    private String flightId;
}
