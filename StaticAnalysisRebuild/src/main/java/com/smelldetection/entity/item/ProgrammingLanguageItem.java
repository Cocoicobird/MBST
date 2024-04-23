package com.smelldetection.entity.item;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 语言使用情况
 */
@Data
public class ProgrammingLanguageItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 语言名称
     */
    private String languageName;
    /**
     * 比特
     */
    private Double bytes;
    /**
     * 使用该语言的文件列表
     */
    private List<String> fileList;

    public ProgrammingLanguageItem() {
        this.fileList = new ArrayList<>();
    }
}
