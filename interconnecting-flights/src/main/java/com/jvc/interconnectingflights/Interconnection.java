package com.jvc.interconnectingflights;

import java.util.List;

import com.jvc.interconnectingflights.webapp.Leg;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Interconnection {
    private int stops;
    private List<Leg> legs;
}
