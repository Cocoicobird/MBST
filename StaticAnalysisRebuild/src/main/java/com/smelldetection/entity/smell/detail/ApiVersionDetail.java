package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class ApiVersionDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean status;
    private String time;
    /**
     * key 为微服务名称
     * value 为对应方法和 url
     */
    private Map<String, Map<String, String>> noVersion;
    private Map<String, List<String>> missingUrl;

    public ApiVersionDetail() {
        this.noVersion = new HashMap<>();
        this.missingUrl = new HashMap<>();
    }
}
