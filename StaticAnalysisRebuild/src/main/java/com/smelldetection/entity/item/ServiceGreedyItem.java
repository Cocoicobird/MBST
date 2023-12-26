package com.smelldetection.entity.item;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class ServiceGreedyItem {
    private String microserviceName;
    private List<String> staticFiles;

    public ServiceGreedyItem() {
        this.staticFiles = new ArrayList<>();
    }
}
