package com.smelldetection.entity.smell.detail;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class SharedDatabasesAndServiceIntimacyDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String time;
    private Map<String, List<String>> sharedDatabases; // key 为数据库 value 为使用该数据库的微服务集合
    public Map<String, List<String>> serviceIntimacy; // key 为微服务 value 为与其亲密的微服务


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
