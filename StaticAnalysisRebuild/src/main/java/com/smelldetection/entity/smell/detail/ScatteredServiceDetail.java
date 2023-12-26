package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class ScatteredServiceDetail {
    private boolean status;
    private List<Set<String>> scatteredServices;

    public ScatteredServiceDetail() {
        scatteredServices = new ArrayList<>();
    }
}