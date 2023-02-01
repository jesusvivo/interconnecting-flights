package com.jvc.interconnectingflights.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

public class RouteRetriever {
    private WebClient webClient;
    private Flux<Route> routesFlux;

    private static final String API_URL = "https://services-api.ryanair.com";

    public RouteRetriever() {
        this.webClient = WebClient.create(API_URL);
        this.routesFlux = webClient.get()
                .uri("/locate/3/routes")
                .retrieve()
                .bodyToFlux(Route.class)
                .filter(route -> route.getConnectingAirport() == null && route.getOperator().equals("RYANAIR"));
    }

    public List<Route> getDirectRoutes(String departure, String arrival) {
        return routesFlux
                .filter(route -> route.getAirportFrom().equals(departure) && route.getAirportTo().equals(arrival))
                .collectList()
                .block();
    }

    public List<Route> getIndirectRoutes(String departure, String arrival) {
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
}
