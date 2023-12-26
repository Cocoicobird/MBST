package com.smelldetection.entity.smell.detail;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class SharedLibraryDetail {
    private Map<String, Set<String>> sharedLibraries;

    public SharedLibraryDetail() {
        this.sharedLibraries = new HashMap<>();
    }

    public boolean contains(String key) {
        return this.sharedLibraries.containsKey(key);
    }

    public void put(String key, String value) {
        if (!contains(key)) {
            this.sharedLibraries.put(key, new LinkedHashSet<>());
        }
        this.sharedLibraries.get(key).add(value);
    }
}
