package com.jvc.interconnectingflights;

import java.util.List;

import lombok.Data;

@Data
public class Interconnection {
    private List<Flight> legs;
}
