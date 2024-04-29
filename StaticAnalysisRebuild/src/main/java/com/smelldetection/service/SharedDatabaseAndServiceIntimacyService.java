package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.ScatteredServiceDetail;
import com.smelldetection.entity.smell.detail.SharedDatabasesAndServiceIntimacyDetail;
import com.smelldetection.entity.system.component.Configuration;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 共享数据库（一个数据库被多个微服务模块访问）和服务亲密（一个微服务模块访问其他模块数据库）
 */
@Service
public class SharedDatabaseAndServiceIntimacyService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public SharedDatabasesAndServiceIntimacyDetail getSharedDatabasesAndServiceIntimacy(Map<String, Configuration> configurations, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(start);
        String pattern = "mysql://";
        // key 为数据库 value 为使用该数据库的微服务模块
        Map<String, List<String>> sharedDatabases = new HashMap<>();
        // key 为微服务模块 value 为其数据库
        Map<String, List<String>> serviceIntimacy = new HashMap<>(); // 该微服务模块使用的数据库
        for (String microserviceName : configurations.keySet()) {
            Configuration configuration = configurations.get(microserviceName);
            System.out.println(microserviceName + " " + configuration);
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
        sharedDatabasesAndServiceIntimacyDetail.setTime(time);
        System.out.println(sharedDatabases);
        for (String database : sharedDatabases.keySet()) {
            if (sharedDatabases.get(database).size() > 1) { // 该数据库被多个服务使用
                sharedDatabasesAndServiceIntimacyDetail.addSharedDatabases(database, sharedDatabases.get(database));
            }
        }
        for (String microservice : serviceIntimacy.keySet()) {
            List<String> databases = serviceIntimacy.get(microservice);
            List<String> microservices = new ArrayList<>();
            for (String database : databases) {
                for (String ms : sharedDatabases.get(database))
                    if (!microservices.contains(ms) && !microservice.equals(ms))
                        microservices.add(ms);
            }
            if (!microservices.isEmpty())
                sharedDatabasesAndServiceIntimacyDetail.addServiceIntimacy(microservice, microservices);
        }
        redisTemplate.opsForValue().set(systemPath + "_sharedDatabasesAndServiceIntimacy_" + start, sharedDatabasesAndServiceIntimacyDetail);
        return sharedDatabasesAndServiceIntimacyDetail;
    }

    public List<SharedDatabasesAndServiceIntimacyDetail> getSharedDatabasesAndServiceIntimacyHistory(String systemPath) {
        String key = systemPath + "_sharedDatabasesAndServiceIntimacy_*";
        Set<String> keys = redisTemplate.keys(key);
        List<SharedDatabasesAndServiceIntimacyDetail> sharedDatabasesAndServiceIntimacyDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                sharedDatabasesAndServiceIntimacyDetails.add((SharedDatabasesAndServiceIntimacyDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return sharedDatabasesAndServiceIntimacyDetails;
    }
}
