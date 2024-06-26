package com.smelldetection.entity.item;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class BloatedServiceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer maxParameterNumber; // 方法最大参数量
    private Double microserviceCallPercent; // 调用的微服务数量占总微服务数量的比重
    private Integer totalServiceImplMethodCallCount; // 控制器层调用业务层的总调用次数
    private Integer codeSize; // 代码行数
}
