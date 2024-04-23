package com.smelldetection.entity.item;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 微服务实体类数量
 */
@Data
public class ServiceCutItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String microserviceName;
    private Integer entityCount;
}
