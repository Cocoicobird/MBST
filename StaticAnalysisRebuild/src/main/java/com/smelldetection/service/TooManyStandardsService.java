package com.smelldetection.service;

import com.smelldetection.entity.item.ProgrammingLanguageItem;
import com.smelldetection.entity.smell.detail.TooManyStandardsDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.ProgrammingLanguageUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class TooManyStandardsService {

    /**
     * @param directory 整个微服务项目
     * @return
     */
    public TooManyStandardsDetail TooManyStandards(String directory) throws IOException {
        TooManyStandardsDetail tooManyStandardsDetail = new TooManyStandardsDetail();
        List<String> services = FileUtils.getServices(directory);
        // 针对每个微服务模块进行分析
        for (String service : services) {
            // 获取该微服务模块的语言使用情况
            Map<String, ProgrammingLanguageItem> programmingLanguages = ProgrammingLanguageUtils.analyseProgrammingLanguage(service);
            // 针对该微服务模块的语言使用情况对整个系统的语言使用情况进行更新
            for (String programmingLanguage : programmingLanguages.keySet()) {
                tooManyStandardsDetail.updateProgrammingLanguageUsage(programmingLanguage,
                        programmingLanguages.get(programmingLanguage));
            }
        }
        return tooManyStandardsDetail;
    }
}
