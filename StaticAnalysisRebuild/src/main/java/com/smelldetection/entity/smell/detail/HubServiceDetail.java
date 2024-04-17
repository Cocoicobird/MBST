package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.DependCount;
import lombok.Data;

import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class HubServiceDetail {
    Map<String, DependCount> hubClasses;
    public void put(String microserviceName, DependCount dependCount) {
        hubClasses.put(microserviceName, dependCount);
    }
}
