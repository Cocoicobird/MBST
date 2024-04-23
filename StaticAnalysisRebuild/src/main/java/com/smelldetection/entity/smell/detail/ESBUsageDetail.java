package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ServiceCallItem;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class ESBUsageDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private Map<String, ServiceCallItem> serviceCallItems;

    public ESBUsageDetail() {
        serviceCallItems = new HashMap<>();
    }
}
