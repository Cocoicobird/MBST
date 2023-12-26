package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ServiceCallItem;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class ESBUsageDetail {
    private boolean status;
    private Map<String, ServiceCallItem> serviceCallItems;

    public ESBUsageDetail() {
        serviceCallItems = new HashMap<>();
    }
}
