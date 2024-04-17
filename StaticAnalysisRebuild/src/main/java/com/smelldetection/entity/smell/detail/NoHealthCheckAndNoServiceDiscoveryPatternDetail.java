package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class NoHealthCheckAndNoServiceDiscoveryPatternDetail {
    Map<String, Boolean> hasDiscovery;

    public NoHealthCheckAndNoServiceDiscoveryPatternDetail() {
        this.hasDiscovery = new HashMap<>();
    }

    public void put(String microserviceName, Boolean status) {
        this.hasDiscovery.put(microserviceName, status);
    }
}
