package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class NoHealthCheckAndNoServiceDiscoveryPatternDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private String time;
    Map<String, Boolean> hasDiscovery;

    public NoHealthCheckAndNoServiceDiscoveryPatternDetail() {
        this.hasDiscovery = new HashMap<>();
    }

    public void put(String microserviceName, Boolean status) {
        this.hasDiscovery.put(microserviceName, status);
    }
}
