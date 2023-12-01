package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.HardCodeItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 某一微服务的硬编码信息
 */
@Data
public class HardCodeDetail {
    private String microserviceName;
    private List<HardCodeItem> hardCodes;

    public HardCodeDetail() {
        this.hardCodes = new ArrayList<>();
    }

    public void add(HardCodeItem hardCodeItem) {
        this.hardCodes.add(hardCodeItem);
    }
}
