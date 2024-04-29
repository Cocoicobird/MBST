package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 存储 WhateverTypes 异味相关信息
 * 形式为 Map 的 key 为微服务名称 value 类名 + 方法
 */
@Data
public class WhateverTypesDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private String time;
    private Map<String, List<String>> returnTypes;

    public WhateverTypesDetail() {
        this.returnTypes = new HashMap<>();
    }

    public void put(String microserviceName, String returnType) {
        if (!this.returnTypes.containsKey(microserviceName))
            this.returnTypes.put(microserviceName, new ArrayList<>());
        this.returnTypes.get(microserviceName).add(returnType);
    }
}
