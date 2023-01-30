package com.jvc.interconnectingflights.webapp;

import java.util.List;

import com.jvc.interconnectingflights.Flight;

import lombok.Data;

@Data
public class DailySchedule {
    private int day;
    private List<Flight> flights;
}
