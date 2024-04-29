package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.BloatedServiceItem;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class BloatedServiceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status; // true 表示为 BloatedService
    private String time;
    Map<String, BloatedServiceItem> bloatedServices;

    public BloatedServiceDetail() {
        this.bloatedServices = new HashMap<>();
    }

    public void put(String microserviceName, BloatedServiceItem bloatedServiceItem) {
        this.bloatedServices.put(microserviceName, bloatedServiceItem);
    }
}
