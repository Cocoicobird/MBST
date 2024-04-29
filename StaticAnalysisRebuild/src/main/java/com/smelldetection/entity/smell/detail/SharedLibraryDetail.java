package com.smelldetection.entity.smell.detail;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class SharedLibraryDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private String time;
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
