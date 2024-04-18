package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.LocalLoggingDetail;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.utils.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class LocalLoggingService {

    public LocalLoggingDetail getLocalLoggingService(Map<String, String> filePathToMicroserviceName) throws IOException, DocumentException {
        LocalLoggingDetail localLoggingDetail = new LocalLoggingDetail();
        Map<String, Configuration> filePathToConfiguration = FileUtils.getConfiguration(filePathToMicroserviceName);
        SAXReader saxReader = new SAXReader();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> logs = FileUtils.getLogs(filePath);
            boolean logstash = false;
            for (String log : logs) {
                Document document = saxReader.read(new File(log));
                Element rootElement = document.getRootElement();
                for (Element element : rootElement.elements()) {
                    if ("appender".equals(element.getName()) && "net.logstash.logback.appender.LogstashTcpSocketAppender".equals(element.attributeValue("class"))) {
                        logstash = true;
                        break;
                    }
                }
            }
            boolean status = false;
            for (String key : filePathToConfiguration.get(filePath).getItems().keySet()) {
                if (key.contains("log") || !(logs.size() > 0 && logstash)) {
                    status = true;
                    break;
                }
            }
            System.out.println(filePath + " " + logstash);
            localLoggingDetail.put(microserviceName, status);
        }
        return localLoggingDetail;
    }

}
