package com.jvc.interconnectingflights.webapp;

import java.util.List;

import lombok.Data;

@Data
public class MonthlySchedule {
    private int month;
    private List<DailySchedule> days;
}
