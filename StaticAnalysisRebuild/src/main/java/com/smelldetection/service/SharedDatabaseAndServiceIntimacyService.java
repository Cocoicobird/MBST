package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.SharedDatabasesAndServiceIntimacyDetail;
import com.smelldetection.entity.system.component.Configuration;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 共享数据库和服务亲密
 */
@Service
public class SharedDatabaseAndServiceIntimacyService {

    public Set<String> getSharedDatabasesAndServiceIntimacy(List<Configuration> configurations) throws IOException {
        String pattern = "mysql://";
        Map<String, List<String>> sharedDatabases = new HashMap<>();
        Map<String, List<String>> serviceIntimacy = new HashMap<>();
        for (Configuration configuration : configurations) {
            for (String key : configuration.getItems().keySet()) {
                String value = configuration.get(key);
                String database = "";
                if (value.contains(pattern)) {
                    int startIndex = value.indexOf(pattern) + 8;
                    int endIndex = value.contains("?") ? value.indexOf("?") : value.length();
                    if (value.contains("///")) {
                        startIndex = value.indexOf("///") + 3;
                        database = "localhost:3306/" + value.substring(startIndex, endIndex);
                    }
                    else if (value.contains("127.0.0.1")){
                        startIndex = value.indexOf("//") + 2;
                        database = value.substring(startIndex, endIndex);
                        database = database.replace("localhost", "127.0.0.1");
                    }
                    else {
                        database = value.substring(startIndex, endIndex);
                    }
                    if (!sharedDatabases.containsKey(database)) {
                        sharedDatabases.put(database, new ArrayList<>());
                    }
                    sharedDatabases.get(database).add(configuration.getMicroserviceName());
                    if (!serviceIntimacy.containsKey(database)) {
                        serviceIntimacy.put(database, new ArrayList<>());
                    }
                    serviceIntimacy.get(database).add(configuration.getMicroserviceName());
                }
            }
        }
        SharedDatabasesAndServiceIntimacyDetail sharedDatabasesAndServiceIntimacyDetail = new SharedDatabasesAndServiceIntimacyDetail();
        Set<String> keys = sharedDatabases.keySet();
        for (String database : keys) {
            if (sharedDatabases.get(database).size() > 1) {
                boolean shared = true;
                List<String> microservices = sharedDatabases.get(database);
                for (String microservice : microservices) {
                    if (serviceIntimacy.get(microservice).size() > 1) {
                        shared = false;
                    }
                    break;
                }
                if (shared) {
                    sharedDatabasesAndServiceIntimacyDetail.addSharedDatabases(database, microservices);
                } else {
                    sharedDatabasesAndServiceIntimacyDetail.addServiceIntimacy(database, microservices);
                }
            }
        }
        /** TODO
         * return sharedDatabasesAndServiceIntimacyDetail;
         */
        return new LinkedHashSet<>();
    }
}
