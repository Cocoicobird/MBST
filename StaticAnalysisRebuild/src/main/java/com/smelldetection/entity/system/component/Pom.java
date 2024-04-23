package com.smelldetection.entity.system.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.maven.model.Model;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description Pom文件实体类，存储每个微服务的依赖
 */
@Data
@AllArgsConstructor
public class Pom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String microserviceName;

    private Model mavenModel;

    public Pom() { }
}
