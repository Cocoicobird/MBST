package com.smelldetection.entity.item;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class DependCount implements Serializable {
    private static final long serialVersionUID = 1L;

    private String className;
    private String microserviceName; // 所属的微服务模块
    private Integer importCount; // 引入的类
    private Integer outputCount; // 被引用的次数

    public DependCount(String className) {
        this.className = className;
        this.importCount = 0;
        this.outputCount = 0;
    }

    public DependCount(DependCount dependCount) {
        if (dependCount != null) {
            this.className = dependCount.getClassName();
            this.microserviceName = dependCount.getMicroserviceName();
            this.importCount = dependCount.getImportCount();
            this.outputCount = dependCount.getOutputCount();
        }
    }

    public boolean equals(DependCount dependCount) {
        return dependCount.getClassName().equals(className)
                && dependCount.getMicroserviceName().equals(microserviceName);
    }
}
