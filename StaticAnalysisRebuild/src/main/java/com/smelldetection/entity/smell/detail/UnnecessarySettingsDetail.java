package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class UnnecessarySettingsDetail {
    private Map<String, List<String>> microserviceNameToConfigurations;

    public UnnecessarySettingsDetail() {
        this.microserviceNameToConfigurations = new HashMap<>();
    }

    public void put(String microserviceName, String configuration) {
        if (!this.microserviceNameToConfigurations.containsKey(microserviceName)) {
            this.microserviceNameToConfigurations.put(microserviceName, new ArrayList<>());
        }
        this.microserviceNameToConfigurations.get(microserviceName).add(configuration);
    }
}
