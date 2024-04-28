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
public class ApiDesignDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * key 为微服务名称
     * value 为对应方法和 url
     */
    private boolean status;
    private Map<String, Map<String, String>> noVersion;
    private Map<String, Map<String, String>> noStandard;
    private Map<String, Map<String, String>> missingHttpMethod;

    public ApiDesignDetail() {
        this.noVersion = new HashMap<>();
        this.noStandard = new HashMap<>();
        this.missingHttpMethod = new HashMap<>();
    }

    public void putNoVersion(String microserviceName, String methodName, String url) {
        if (!noVersion.containsKey(microserviceName)) {
            noVersion.put(microserviceName, new HashMap<>());
        }
        noVersion.get(microserviceName).put(methodName, url);
    }

    public void putNoStandard(String microserviceName, String methodName, String url) {
        if (!noStandard.containsKey(microserviceName)) {
            noStandard.put(microserviceName, new HashMap<>());
        }
        noStandard.get(microserviceName).put(methodName, url);
    }

    public void putMissingHttpMethod(String microserviceName, String methodName, String url) {
        if (!missingHttpMethod.containsKey(microserviceName)) {
            missingHttpMethod.put(microserviceName, new HashMap<>());
        }
        missingHttpMethod.get(microserviceName).put(methodName, url);
    }
}
