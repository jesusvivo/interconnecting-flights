package com.jvc.interconnectingflights;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RouteController {
    private final WebClient webClient;

    private final Logger logger;

    public RouteController() {
        this.webClient = WebClient.create("https://services-api.ryanair.com/locate/3/routes");
        this.logger = LoggerFactory.getLogger(RouteController.class);
    }

    @GetMapping("/interconnections")
    public Flux<Route> getDirectRoutes() {
        try {
            return webClient.get()
                    .retrieve()
                    .bodyToFlux(Route.class)
                    .filter(route -> route.getConnectingAirport() == null && route.getOperator().equals("RYANAIR"));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

}
