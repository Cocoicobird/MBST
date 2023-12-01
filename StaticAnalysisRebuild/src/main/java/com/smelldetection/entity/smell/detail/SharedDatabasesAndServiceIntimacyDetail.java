package com.smelldetection.entity.smell.detail;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class SharedDatabasesAndServiceIntimacyDetail {
    private Map<String, List<String>> sharedDatabases;
    public Map<String, List<String>> serviceIntimacy;


    public SharedDatabasesAndServiceIntimacyDetail() {
        this.sharedDatabases = new HashMap<>();
        this.serviceIntimacy = new HashMap<>();
    }

    public void addSharedDatabases(String database, List<String> microservices){
        this.sharedDatabases.put(database, microservices);
    }

    public void addServiceIntimacy(String database, List<String> microservices){
        this.serviceIntimacy.put(database, microservices);
    }

}
