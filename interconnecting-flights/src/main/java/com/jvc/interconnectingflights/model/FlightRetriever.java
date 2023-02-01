package com.jvc.interconnectingflights.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;

public class FlightRetriever {
        private WebClient webClient;

        private static final String API_URL = "https://services-api.ryanair.com";

        public FlightRetriever() {
                this.webClient = WebClient.create(API_URL);
        }

        public List<Flight> getDirectFlights(String departure, String arrival, String departureDateTime,
                        String arrivalDateTime) {
                LocalDateTime inputDepartureTime = LocalDateTime.parse(departureDateTime,
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime inputArrivalTime = LocalDateTime.parse(arrivalDateTime,
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                return webClient.get()
                                .uri("/timtbl/3/schedules/" + departure + "/" + arrival + "/years/"
                                                + inputDepartureTime.getYear()
                                                + "/months/" + inputDepartureTime.getMonthValue())
                                .retrieve()
                                .bodyToFlux(MonthlySchedule.class)
                                .flatMapIterable(MonthlySchedule::getDays)
                                .filter(day -> day.getDay() == inputDepartureTime.getDayOfMonth())
                                .flatMapIterable(DailySchedule::getFlights)
                                .filter(flight -> {
                                        LocalTime flightDepartureHourAndMinute = LocalTime
                                                        .parse(flight.getDepartureTime());
                                        LocalTime flightArrivalHourAndMinute = LocalTime.parse(flight.getArrivalTime());

                                        return flightDepartureHourAndMinute.isAfter(inputDepartureTime.toLocalTime())
                                                        && flightArrivalHourAndMinute
                                                                        .isBefore(inputArrivalTime.toLocalTime());
                                })
                                .collectList()
                                .block();
        }

        public void addDirectFlights(List<Interconnection> interconnections, List<Route> directRoutes,
                        String departureDateTime,
                        String arrivalDateTime) {
                for (Route directRoute : directRoutes) {
                        List<Flight> directFlights = getDirectFlights(directRoute.getAirportFrom(),
                                        directRoute.getAirportTo(), departureDateTime, arrivalDateTime);

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

        public void addInterconnectedFlights(List<Interconnection> interconnections, List<Route> indirectRoutes,
                        String departureDateTime, String arrivalDateTime) {
                for (Route indirectRoute : indirectRoutes) {
                        List<Flight> departureToConnectingFlights = getDirectFlights(indirectRoute.getAirportFrom(),
                                        indirectRoute.getConnectingAirport(), departureDateTime, arrivalDateTime);

                        List<Flight> connectingToArrivalFlights = getDirectFlights(indirectRoute.getConnectingAirport(),
                                        indirectRoute.getAirportTo(), departureDateTime, arrivalDateTime);

                        for (Flight firstFlight : departureToConnectingFlights) {
                                for (Flight secondFlight : connectingToArrivalFlights) {
                                        LocalTime firstFlightArrivalTime = LocalTime
                                                        .parse(firstFlight.getArrivalTime());
                                        LocalTime secondFlightDepartureTime = LocalTime
                                                        .parse(secondFlight.getDepartureTime());

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
