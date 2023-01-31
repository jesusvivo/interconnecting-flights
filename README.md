# Interconnecting Flights

Spring Boot application that returns a list of flights departing from a given departure airport not earlier than the specified departure datetime and arriving
to a given arrival airport not later than the specified arrival datetime.

The returned list consists of:
- all direct flights if available (for example: DUB - WRO)
- all interconnected flights with a maximum of one stop if available (for example: DUB - STN - WRO). The time difference between the arrival and the next departure is 2h or
greater

The application consumes data from the following two microservices:

- Routes API: https://services-api.ryanair.com/locate/3/routes which returns a list of all available routes based on the airport's IATA codes.
- Schedules API: https://servicesapi.ryanair.com/timtbl/3/schedules/{departure}/{arrival}/years/{year}/months/{month} which returns a list of available flights for a given departure airport IATA code, an arrival airport IATA code, a year
and a month

After running the JAR file, the application can be accesed at:
http://localhost:8080/ryanair/interconnections?departure={departure}&arrival={arrival}&departureDateTime={departureDateTime}&arrivalDateTime={arrivalDateTime}

For example: http://localhost:8080/ryanair/interconnections?departure=MAD&arrival=BGY&departureDateTime=2023-06-01T07:00&arrivalDateTime=2023-06-03T21:00
