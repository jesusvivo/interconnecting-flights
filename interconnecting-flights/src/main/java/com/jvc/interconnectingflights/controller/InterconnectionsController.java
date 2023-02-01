package com.jvc.interconnectingflights.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jvc.interconnectingflights.model.FlightRetriever;
import com.jvc.interconnectingflights.model.Interconnection;
import com.jvc.interconnectingflights.model.Route;
import com.jvc.interconnectingflights.model.RouteRetriever;

@RestController
public class InterconnectionsController {

    @GetMapping("/interconnections")
    public List<Interconnection> getInterconnections(
            @RequestParam @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]+$") String departure,
            @RequestParam @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]+$") String arrival,
            @RequestParam @NotBlank @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") String departureDateTime,
            @RequestParam @NotBlank @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") String arrivalDateTime) {

        List<Interconnection> interconnections = new ArrayList<Interconnection>();
        RouteRetriever routeRetriever = new RouteRetriever();
        FlightRetriever flightRetriever = new FlightRetriever();

        List<Route> directRoutes = routeRetriever.getDirectRoutes(departure, arrival);

        flightRetriever.addDirectFlights(interconnections, directRoutes, departureDateTime, arrivalDateTime);

        List<Route> indirectRoutes = routeRetriever.getIndirectRoutes(departure, arrival);

        flightRetriever.addInterconnectedFlights(interconnections, indirectRoutes, departureDateTime, arrivalDateTime);

        return interconnections;
    }
}
