package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 存储 WhateverTypes 异味相关信息
 * 形式为 1.微服务模块名 2.Map 的 key 为 类名 + 方法 字符串 value 为 返回类型
 */
@Data
public class WhateverTypesDetail {
    private String microserviceName;
    private Map<String, String> returnTypes;

    public WhateverTypesDetail() {
        this.returnTypes = new HashMap<>();
    }

    public void put(String method, String returnType) {
        this.returnTypes.put(method, returnType);
    }

    public boolean isEmpty() {
        return returnTypes.isEmpty();
    }
}
