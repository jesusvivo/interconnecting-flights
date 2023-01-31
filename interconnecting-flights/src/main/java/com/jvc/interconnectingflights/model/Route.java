package com.jvc.interconnectingflights.model;

import lombok.Data;

@Data
public class Route {
    private String airportFrom;
    private String airportTo;
    private String connectingAirport;
    private boolean newRoute;
    private boolean seasonalRoute;
    private String operator;
    private String group;
}
