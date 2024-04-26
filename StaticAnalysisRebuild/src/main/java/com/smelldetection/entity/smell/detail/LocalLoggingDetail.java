package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class LocalLoggingDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private Map<String, Boolean> logs; // true 为 Local Logging

    public LocalLoggingDetail() {
        this.logs = new HashMap<>();
    }

    public void put(String microserviceName, Boolean status) {
        this.logs.put(microserviceName, status);
    }
}
