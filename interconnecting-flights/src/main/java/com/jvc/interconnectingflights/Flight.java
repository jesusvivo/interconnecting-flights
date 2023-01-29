package com.jvc.interconnectingflights;

import lombok.Data;

@Data
public class Flight {
    private String departureAirport;
    private String arrivalAirport;
    private String departureDateTime;
    private String arrivalDateTime;
}
