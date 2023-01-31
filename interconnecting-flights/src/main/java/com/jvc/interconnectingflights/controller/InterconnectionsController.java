package com.jvc.interconnectingflights.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.jvc.interconnectingflights.model.DailySchedule;
import com.jvc.interconnectingflights.model.Flight;
import com.jvc.interconnectingflights.model.Interconnection;
import com.jvc.interconnectingflights.model.Leg;
import com.jvc.interconnectingflights.model.MonthlySchedule;
import com.jvc.interconnectingflights.model.Route;

import reactor.core.publisher.Flux;

@RestController
public class InterconnectionsController {

    private final WebClient webClient;
    private Flux<Route> routesFlux;

    public InterconnectionsController() {
        this.webClient = WebClient.create("https://services-api.ryanair.com");
    }

    @GetMapping("/interconnections")
    public List<Interconnection> getInterconnections(
            @RequestParam @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]+$") String departure,
            @RequestParam @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]+$") String arrival,
            @RequestParam @NotBlank @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") String departureDateTime,
            @RequestParam @NotBlank @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") String arrivalDateTime) {

        List<Interconnection> interconnections = new ArrayList<Interconnection>();

        routesFlux = webClient.get()
                .uri("/locate/3/routes")
                .retrieve()
                .bodyToFlux(Route.class)
                .filter(route -> route.getConnectingAirport() == null && route.getOperator().equals("RYANAIR"));

        List<Route> directRoutes = getDirectRoutes(departure, arrival);

        addDirectFlights(interconnections, directRoutes, departureDateTime, arrivalDateTime);

        List<Route> indirectRoutes = getIndirectRoutes(departure, arrival);

        addInterconnectedFlights(interconnections, indirectRoutes, departureDateTime, arrivalDateTime);

        return interconnections;
    }

    List<Route> getDirectRoutes(String departure, String arrival) {
        return routesFlux
                .filter(route -> route.getAirportFrom().equals(departure) && route.getAirportTo().equals(arrival))
                .collectList()
                .block();
    }

    List<Flight> getDirectFlights(String departure, String arrival, String departureDateTime, String arrivalDateTime) {
        LocalDateTime inputDepartureTime = LocalDateTime.parse(departureDateTime,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime inputArrivalTime = LocalDateTime.parse(arrivalDateTime,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return webClient.get()
                .uri("/timtbl/3/schedules/" + departure + "/" + arrival + "/years/" + inputDepartureTime.getYear()
                        + "/months/" + inputDepartureTime.getMonthValue())
                .retrieve()
                .bodyToFlux(MonthlySchedule.class)
                .flatMapIterable(MonthlySchedule::getDays)
                .filter(day -> day.getDay() == inputDepartureTime.getDayOfMonth())
                .flatMapIterable(DailySchedule::getFlights)
                .filter(flight -> {
                    LocalTime flightDepartureHourAndMinute = LocalTime.parse(flight.getDepartureTime());
                    LocalTime flightArrivalHourAndMinute = LocalTime.parse(flight.getArrivalTime());

                    return flightDepartureHourAndMinute.isAfter(inputDepartureTime.toLocalTime())
                            && flightArrivalHourAndMinute.isBefore(inputArrivalTime.toLocalTime());
                })
                .collectList()
                .block();
    }

    void addDirectFlights(List<Interconnection> interconnections, List<Route> directRoutes, String departureDateTime,
            String arrivalDateTime) {
        for (Route directRoute : directRoutes) {
            List<Flight> directFlights = getDirectFlights(directRoute.getAirportFrom(), directRoute.getAirportTo(),
                    departureDateTime, arrivalDateTime);

            for (Flight directFlight : directFlights) {
                List<Leg> legs = new ArrayList<Leg>();

                Leg leg = new Leg(
                        directRoute.getAirportFrom(),
                        directRoute.getAirportTo(),
                        directFlight.getDepartureTime(),
                        directFlight.getArrivalTime());

                legs.add(leg);

                Interconnection interconnection = new Interconnection(0, legs);

                interconnections.add(interconnection);
            }
        }
    }

    List<Route> getIndirectRoutes(String departure, String arrival) {
        Map<String, List<String>> graph = new HashMap<>();

        List<Route> allRoutes = routesFlux.collectList().block();

        for (Route route : allRoutes) {
            String airportFrom = route.getAirportFrom();
            String airportTo = route.getAirportTo();

            if (!graph.containsKey(airportFrom)) {
                graph.put(airportFrom, new ArrayList<>());
            }

            graph.get(airportFrom).add(airportTo);
        }

        List<Route> indirectRoutes = new ArrayList<Route>();

        for (String airport : graph.keySet()) {
            if (graph.get(departure) != null && graph.get(airport) != null &&
                    graph.get(departure).contains(airport) && graph.get(airport).contains(arrival)) {
                Route indirectRoute = new Route();

                indirectRoute.setAirportFrom(departure);
                indirectRoute.setAirportTo(arrival);
                indirectRoute.setConnectingAirport(airport);
                indirectRoutes.add(indirectRoute);
            }
        }

        return indirectRoutes;
    }

    void addInterconnectedFlights(List<Interconnection> interconnections, List<Route> indirectRoutes,
            String departureDateTime, String arrivalDateTime) {
        for (Route indirectRoute : indirectRoutes) {
            List<Flight> departureToConnectingFlights = getDirectFlights(indirectRoute.getAirportFrom(),
                    indirectRoute.getConnectingAirport(), departureDateTime, arrivalDateTime);

            List<Flight> connectingToArrivalFlights = getDirectFlights(indirectRoute.getConnectingAirport(),
                    indirectRoute.getAirportTo(), departureDateTime, arrivalDateTime);

            for (Flight firstFlight : departureToConnectingFlights) {
                for (Flight secondFlight : connectingToArrivalFlights) {
                    LocalTime firstFlightArrivalTime = LocalTime.parse(firstFlight.getArrivalTime());
                    LocalTime secondFlightDepartureTime = LocalTime.parse(secondFlight.getDepartureTime());

                    if (firstFlightArrivalTime.plusHours(2).isBefore(secondFlightDepartureTime)) {
                        List<Leg> legs = new ArrayList<Leg>();

                        Leg firstLeg = new Leg(
                                indirectRoute.getAirportFrom(),
                                indirectRoute.getConnectingAirport(),
                                firstFlight.getDepartureTime(),
                                firstFlight.getArrivalTime());

                        Leg secondLeg = new Leg(
                                indirectRoute.getConnectingAirport(),
                                indirectRoute.getAirportTo(),
                                secondFlight.getDepartureTime(),
                                secondFlight.getArrivalTime());

                        legs.add(firstLeg);
                        legs.add(secondLeg);

                        Interconnection interconnection = new Interconnection(1, legs);

                        interconnections.add(interconnection);
                    }
                }
            }
        }
    }

}
