
package com.jvc.interconnectingflights.webapp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.jvc.interconnectingflights.Flight;
import com.jvc.interconnectingflights.Interconnection;
import com.jvc.interconnectingflights.Route;

@RestController
public class Controller {

    private final WebClient webClient;

    public Controller() {
        this.webClient = WebClient.create("https://services-api.ryanair.com");
    }

    @GetMapping("/interconnections")
    public List<Interconnection> getInterconnections(@RequestParam String departure,
            @RequestParam String arrival, @RequestParam String departureDateTime,
            @RequestParam String arrivalDateTime) {

        List<Interconnection> interconnections = new ArrayList<Interconnection>();
        List<Route> directRoutes = getDirectRoutes(departure, arrival);

        for (Route route : directRoutes) {
            List<Flight> directFlights = getDirectFlights(route.getAirportFrom(), route.getAirportTo(),
                    departureDateTime, arrivalDateTime);

            for (Flight flight : directFlights) {
                List<Leg> legs = new ArrayList<Leg>();
                Leg leg = new Leg(
                        route.getAirportFrom(),
                        route.getAirportTo(),
                        flight.getDepartureTime(),
                        flight.getArrivalTime());
                legs.add(leg);
                Interconnection interconnection = new Interconnection(0, legs);
                interconnections.add(interconnection);

            }
        }

        return interconnections;
    }

    List<Route> getDirectRoutes(String departure, String arrival) {
        return webClient.get()
                .uri("/locate/3/routes")
                .retrieve()
                .bodyToFlux(Route.class)
                .filter(route -> route.getConnectingAirport() == null && route.getOperator().equals("RYANAIR") &&
                        route.getAirportFrom().equals(departure) && route.getAirportTo().equals(arrival))
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
                        + "/months/" + inputDepartureTime.getMonthValue() + "")
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

}
