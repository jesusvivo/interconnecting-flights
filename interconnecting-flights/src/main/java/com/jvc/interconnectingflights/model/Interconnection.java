package com.jvc.interconnectingflights.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Interconnection {
    private int stops;
    private List<Leg> legs;
}
