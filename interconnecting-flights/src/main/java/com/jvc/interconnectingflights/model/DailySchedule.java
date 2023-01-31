package com.jvc.interconnectingflights.model;

import java.util.List;

import lombok.Data;

@Data
public class DailySchedule {
    private int day;
    private List<Flight> flights;
}
