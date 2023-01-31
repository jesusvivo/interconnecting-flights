/*
 * package com.jvc.interconnectingflights;
 * 
 * import java.time.LocalDateTime;
 * import java.time.LocalTime;
 * import java.time.format.DateTimeFormatter;
 * import java.util.ArrayList;
 * import java.util.Collections;
 * import java.util.HashMap;
 * import java.util.HashSet;
 * import java.util.LinkedList;
 * import java.util.List;
 * import java.util.Map;
 * import java.util.Queue;
 * import java.util.Set;
 * 
 * import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory;
 * import org.springframework.web.bind.annotation.GetMapping;
 * import org.springframework.web.bind.annotation.RequestParam;
 * import org.springframework.web.bind.annotation.RestController;
 * import org.springframework.web.reactive.function.client.WebClient;
 * 
 * import com.jvc.interconnectingflights.webapp.DailySchedule;
 * import com.jvc.interconnectingflights.webapp.MonthlySchedule;
 * 
 * import reactor.core.publisher.Flux;
 * 
 * @RestController
 * public class RouteController {
 * private final WebClient webClient;
 * 
 * private final Logger logger;
 * 
 * public RouteController() {
 * this.webClient = WebClient.create("https://services-api.ryanair.com");
 * this.logger = LoggerFactory.getLogger(RouteController.class);
 * }
 * 
 * @GetMapping("/interconnections")
 * public Flux<Route> getDirectRoutes(@RequestParam String departure,
 * 
 * @RequestParam String arrival, @RequestParam String departureDateTime,
 * 
 * @RequestParam String arrivalDateTime) {
 * 
 * Flux<Route> routesFlux = webClient.get()
 * .uri("/locate/3/routes")
 * .retrieve()
 * .bodyToFlux(Route.class)
 * .filter(route -> route.getConnectingAirport() == null &&
 * route.getOperator().equals("RYANAIR"));
 * 
 * List<Route> directRoutes = routesFlux.filter(
 * route -> route.getAirportFrom().equals(departure) &&
 * route.getAirportTo().equals(arrival))
 * .collectList()
 * .block();
 * 
 * Map<String, List<String>> graph = new HashMap<>();
 * 
 * // populate graph with routes
 * for (Route route : directRoutes) {
 * String airportFrom = route.getAirportFrom();
 * String airportTo = route.getAirportTo();
 * if (!graph.containsKey(airportFrom)) {
 * graph.put(airportFrom, new ArrayList<>());
 * }
 * graph.get(airportFrom).add(airportTo);
 * }
 * 
 * List<List<String>> interconnectedRoutes = findRoutes(departure, arrival,
 * graph);
 * 
 * return routesFlux;
 * }
 * 
 * @GetMapping("/schedules")
 * public Flux<Flight> getSchedules(@RequestParam String departure,
 * 
 * @RequestParam String arrival, @RequestParam String departureDateTime,
 * 
 * @RequestParam String arrivalDateTime) {
 * 
 * LocalDateTime inputDepartureTime = LocalDateTime.parse(departureDateTime,
 * DateTimeFormatter.ISO_LOCAL_DATE_TIME);
 * LocalDateTime inputArrivalTime = LocalDateTime.parse(arrivalDateTime,
 * DateTimeFormatter.ISO_LOCAL_DATE_TIME);
 * 
 * Flux<Flight> schedulesFlux = webClient.get()
 * .uri("/timtbl/3/schedules/" + departure + "/" + arrival + "/years/" +
 * inputDepartureTime.getYear()
 * + "/months/" + inputDepartureTime.getMonthValue() + "")
 * .retrieve()
 * .bodyToFlux(MonthlySchedule.class)
 * .flatMapIterable(MonthlySchedule::getDays)
 * .filter(day -> day.getDay() == inputDepartureTime.getDayOfMonth())
 * .flatMapIterable(DailySchedule::getFlights)
 * .filter(flight -> {
 * LocalTime flightDepartureHourAndMinute =
 * LocalTime.parse(flight.getDepartureTime());
 * LocalTime flightArrivalHourAndMinute =
 * LocalTime.parse(flight.getArrivalTime());
 * 
 * return flightDepartureHourAndMinute.isAfter(inputDepartureTime.toLocalTime())
 * && flightArrivalHourAndMinute.isBefore(inputArrivalTime.toLocalTime());
 * });
 * 
 * return schedulesFlux;
 * }
 * 
 * public List<List<String>> findRoutes(String departure, String arrival,
 * Map<String, List<String>> routes) {
 * Queue<List<String>> queue = new LinkedList<>();
 * Set<List<String>> visited = new HashSet<>();
 * List<List<String>> result = new ArrayList<>();
 * 
 * queue.offer(Collections.singletonList(departure));
 * visited.add(Collections.singletonList(departure));
 * 
 * while (!queue.isEmpty()) {
 * List<String> route = queue.poll();
 * 
 * String lastAirport = route.get(route.size() - 1);
 * 
 * if (lastAirport.equals(arrival)) {
 * result.add(route);
 * } else {
 * List<String> connectingAirports = routes.get(lastAirport);
 * if (connectingAirports != null) {
 * for (String connectingAirport : connectingAirports) {
 * List<String> newRoute = new ArrayList<>(route);
 * newRoute.add(connectingAirport);
 * if (!visited.contains(newRoute)) {
 * queue.offer(newRoute);
 * visited.add(newRoute);
 * }
 * }
 * }
 * }
 * }
 * return result;
 * }
 * 
 * }
 */