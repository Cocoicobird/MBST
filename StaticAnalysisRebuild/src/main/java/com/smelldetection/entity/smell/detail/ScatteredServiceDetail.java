package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class ScatteredServiceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private String time;
    // 松散服务集合，若干个服务共同完成一个功能
    private List<Set<String>> scatteredServices;

    public ScatteredServiceDetail() {
        scatteredServices = new ArrayList<>();
    }
}
