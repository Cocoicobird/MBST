package com.smelldetection.entity.smell.detail;

import lombok.Data;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class DuplicatedServiceDetail {
    private String microserviceName;
    private String methodDeclaration;
}
