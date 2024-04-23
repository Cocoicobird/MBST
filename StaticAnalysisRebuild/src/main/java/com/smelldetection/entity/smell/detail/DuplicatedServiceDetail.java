package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class DuplicatedServiceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String microserviceName;
    private String methodDeclaration;
}
