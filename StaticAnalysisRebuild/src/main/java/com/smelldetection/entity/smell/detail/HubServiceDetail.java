package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.DependCount;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class HubServiceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    Map<String, DependCount> hubClasses;

    public HubServiceDetail() {
        this.hubClasses = new HashMap<>();
    }
    public void put(String microserviceName, DependCount dependCount) {
        hubClasses.put(microserviceName, dependCount);
    }
}
