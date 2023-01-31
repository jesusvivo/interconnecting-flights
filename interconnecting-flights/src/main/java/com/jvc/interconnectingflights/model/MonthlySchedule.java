package com.jvc.interconnectingflights.model;

import java.util.List;

import lombok.Data;

@Data
public class MonthlySchedule {
    private int month;
    private List<DailySchedule> days;
}
