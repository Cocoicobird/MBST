package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ProgrammingLanguageItem;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class TooManyStandardsDetail {

    private Map<String, ProgrammingLanguageItem> programmingLanguages;

    public TooManyStandardsDetail() {
        this.programmingLanguages = new HashMap<>();
    }

    public Integer getUsedLanguageNumber() {
        return this.programmingLanguages.size();
    }

    public void updateProgrammingLanguageUsage(String programmingLanguage, ProgrammingLanguageItem programmingLanguageItem) {
        if (this.programmingLanguages.containsKey(programmingLanguage)) {
            // 之前的使用情况
            ProgrammingLanguageItem pre = programmingLanguages.get(programmingLanguage);
            pre.setBytes(pre.getBytes() + programmingLanguageItem.getBytes());
            pre.getFileList().addAll(programmingLanguageItem.getFileList());
            programmingLanguageItem = pre;
        }
        programmingLanguages.put(programmingLanguage, programmingLanguageItem);
    }
}
