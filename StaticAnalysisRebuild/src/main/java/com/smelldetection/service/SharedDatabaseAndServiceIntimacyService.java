package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.SharedDatabasesAndServiceIntimacyDetail;
import com.smelldetection.entity.system.component.Configuration;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 共享数据库（一个数据库被多个微服务模块访问）和服务亲密（一个微服务模块访问其他模块数据库）
 */
@Service
public class SharedDatabaseAndServiceIntimacyService {

    public SharedDatabasesAndServiceIntimacyDetail getSharedDatabasesAndServiceIntimacy(Map<String, Configuration> configurations) throws IOException {
        String pattern = "mysql://";
        // key 为数据库 value 为使用该数据库的微服务模块
        Map<String, List<String>> sharedDatabases = new HashMap<>();
        // key 为微服务模块 value 为其数据库
        Map<String, List<String>> serviceIntimacy = new HashMap<>();
        for (String microserviceName : configurations.keySet()) {
            Configuration configuration = configurations.get(microserviceName);
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
                    if (!serviceIntimacy.containsKey(configuration.getMicroserviceName())) {
                        serviceIntimacy.put(configuration.getMicroserviceName(), new ArrayList<>());
                    }
                    serviceIntimacy.get(configuration.getMicroserviceName()).add(database);
                }
            }
        }
        SharedDatabasesAndServiceIntimacyDetail sharedDatabasesAndServiceIntimacyDetail = new SharedDatabasesAndServiceIntimacyDetail();
        for (String database : sharedDatabases.keySet()) {
            if (sharedDatabases.get(database).size() > 1) {
                sharedDatabasesAndServiceIntimacyDetail.addSharedDatabases(database, sharedDatabases.get(database));
            }
        }
        for (String microservice : serviceIntimacy.keySet()) {
            List<String> databases = serviceIntimacy.get(microservice);
            List<String> microservices = new ArrayList<>();
            for (String database : databases) {
                for (String ms : sharedDatabases.get(database))
                    if (!microservices.contains(ms))
                        microservices.add(ms);
            }
            sharedDatabasesAndServiceIntimacyDetail.addServiceIntimacy(microservice, microservices);
        }
        return sharedDatabasesAndServiceIntimacyDetail;
    }
}
