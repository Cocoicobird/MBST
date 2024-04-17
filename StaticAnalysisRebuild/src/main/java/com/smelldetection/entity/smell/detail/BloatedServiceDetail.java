package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.BloatedServiceItem;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class BloatedServiceDetail {
    private boolean status; // true 表示为 BloatedService
    Map<String, BloatedServiceItem> bloatedServices;

    public BloatedServiceDetail() {
        this.bloatedServices = new HashMap<>();
    }

    public void put(String microserviceName, BloatedServiceItem bloatedServiceItem) {
        this.bloatedServices.put(microserviceName, bloatedServiceItem);
    }
}
