package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ServiceCutItem;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class GodServiceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private String time;
    Map<String, ServiceCutItem> godServices;

    public GodServiceDetail() {
        this.godServices = new HashMap<>();
    }
}
