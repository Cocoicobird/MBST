package com.smelldetection.controller;

import com.smelldetection.entity.Configuration;
import com.smelldetection.utils.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    @GetMapping("/configuration")
    public String configuration(HttpServletRequest request) throws IOException {
        List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(request.getParameter("path"));
        List<Configuration> configurations = new ArrayList<>();
        for (String application : applicationYamlOrProperties) {
            Configuration configuration = new Configuration(new HashMap<>());
            if (application.endsWith("yaml") || application.endsWith("yml")) {
                Yaml yaml = new Yaml();
                Map<String, Object> yml = yaml.load(new FileInputStream(application));
                FileUtils.resolveYaml(new Stack<>(), configuration.getItems(), yml);
            } else {
                FileUtils.resolveProperties(application, configuration.getItems());
            }
            configurations.add(configuration);
            configuration.getItems().forEach((key, value) -> {
                System.out.println(key + "=" + value);
            });
        }
        return "configuration";
    }
}
