package com.smelldetection.entity.item;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class ServiceGreedyItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String microserviceName;
    private List<String> staticFiles;

    public ServiceGreedyItem() {
        this.staticFiles = new ArrayList<>();
    }
}
