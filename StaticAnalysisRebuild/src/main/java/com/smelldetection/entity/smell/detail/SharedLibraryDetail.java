package com.smelldetection.entity.smell.detail;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class SharedLibraryDetail {
    private String service1;
    private String service2;
    private Set<String> sharedLibraries;

    public SharedLibraryDetail(String service1, String service2) {
        this.service1 = service1;
        this.service2 = service2;
        sharedLibraries = new LinkedHashSet<>();
    }
}
