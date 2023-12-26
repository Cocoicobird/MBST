package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceGreedyItem;
import com.smelldetection.entity.smell.detail.ServiceGreedyDetail;
import com.smelldetection.entity.smell.detail.WrongCutDetail;
import com.smelldetection.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 服务贪婪
 */
@Service
public class GreedyService {

    @Autowired
    private WrongCutService wrongCutService;

    public ServiceGreedyDetail getGreedyService(Map<String, String> filePathToMicroserviceName) throws IOException {
        ServiceGreedyDetail serviceGreedyDetail = new ServiceGreedyDetail();
        WrongCutDetail wrongCutDetail = wrongCutService.getWrongCut(filePathToMicroserviceName);
        for (String directory : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(directory);
            List<String> staticFiles = FileUtils.getStaticFiles(directory);
            if (staticFiles != null && !staticFiles.isEmpty() && staticFiles.size() <= 2) {
                serviceGreedyDetail.addGreedyService(new ServiceGreedyItem(microserviceName, staticFiles));
            } else if (staticFiles != null && staticFiles.isEmpty()
                    && wrongCutDetail.getWrongCutMicroservices().containsKey(microserviceName)
                    && wrongCutDetail.getWrongCutMicroservices().get(microserviceName).getEntityCount() < 1) {
                serviceGreedyDetail.addGreedyService(new ServiceGreedyItem(microserviceName, staticFiles));
            }
        }
        if (!serviceGreedyDetail.getGreedyServices().isEmpty()) {
            serviceGreedyDetail.setExisted(true);
        }
        return serviceGreedyDetail;
    }
}
