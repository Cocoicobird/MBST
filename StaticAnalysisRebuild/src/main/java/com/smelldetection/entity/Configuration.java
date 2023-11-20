package com.smelldetection.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class Configuration {
    private Map<String, String> items;

    public Configuration() {
        this.items = new HashMap<>();
    }

    public String get(String key) {
        return this.items.get(key);
    }

    public void add(String key, String value) {
        this.items.put(key, value);
    }
}
