package com.smelldetection.entity.item;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 存储 .java 文件的 url
 * url1 为类上注解配置的 url
 * url2 为方法上注解配置的 url
 */
@Data
public class UrlItem {
    private String url1;
    private Map<String, String> url2; // key 为方法名 value 为 url
    private Map<String, String> httpMethod; // key 为方法名 value 为对应的 HTTP 方法

    public UrlItem() {
        this.url2 = new HashMap<>();
        this.httpMethod = new HashMap<>();
    }
}
