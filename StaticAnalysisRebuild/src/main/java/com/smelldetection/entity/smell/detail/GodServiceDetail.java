package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ServiceCutItem;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class GodServiceDetail {
    Map<String, ServiceCutItem> godServices;

    public GodServiceDetail() {
        this.godServices = new HashMap<>();
    }
}
