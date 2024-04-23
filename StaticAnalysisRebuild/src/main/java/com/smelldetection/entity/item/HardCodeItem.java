package com.smelldetection.entity.item;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 硬编码的信息
 */
@Data
public class HardCodeItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private String filePath;
    /**
     * key 为硬编码所在文件的行数
     * value 为硬编码内容
     */
    private Map<Integer, String> hardCodeAndPositions;

    public HardCodeItem() {
        this.hardCodeAndPositions = new HashMap<>();
    }
}
