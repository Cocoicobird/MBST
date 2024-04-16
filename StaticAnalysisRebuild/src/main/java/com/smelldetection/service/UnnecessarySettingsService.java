package com.smelldetection.service;

import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 不必要的配置，如配置为默认项可以不进行配置
 * 预加载了 SpringBoot 的一些通用配置
 */
@Service
public class UnnecessarySettingsService {

    private static final Map<String, String> defaultConfiguration = new HashMap<>();

    static {
        try (BufferedReader br = Files.newBufferedReader(Paths.get("StaticAnalysisRebuild", "src", "main", "resources", "configuration.csv"))) {
            // CSV文件的分隔符
            String DELIMITER = ",";
            // 按行读取
            String line;
            while ((line = br.readLine()) != null) {
                // 分割
                String[] columns = line.split(DELIMITER);
                // 打印行
                if (columns.length == 4) {
                    defaultConfiguration.put(columns[1], columns[3]);
                }
                System.out.println("Configuration["+ String.join(", ", columns) +"]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void getUnnecessarySettings(Map<String, String> filePathToMicroserviceName, String systemDirectory) throws IOException {
        Map<String, Configuration> filePathToConfiguration;
        if (redisTemplate.opsForValue().get(systemDirectory + "Configuration") != null) {
            filePathToConfiguration = (Map<String, Configuration>) redisTemplate.opsForValue().get(systemDirectory + "Configuration");
        } else {
            filePathToConfiguration = FileUtils.getConfiguration(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemDirectory + "Configuration", filePathToConfiguration);
        }
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            Configuration configuration = filePathToConfiguration.get(filePath);
            System.out.println(microserviceName);
            for (String key : configuration.getItems().keySet()) {
                String value = configuration.get(key);
                if (defaultConfiguration.containsKey(key) && defaultConfiguration.get(key).equals(value)) {
                    System.out.println(key + ": " + value);
                }
            }
        }
    }
}
