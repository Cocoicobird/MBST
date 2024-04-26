package com.smelldetection.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@TableName("metric_summary")
public class MetricSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelIgnore
    private Long id; // 自增 id
    @ExcelProperty("systemName")
    private String systemName; // 微服务系统名称
    @ExcelProperty("microserviceName")
    private String microserviceName; // 微服务模块名称
    @ExcelProperty("codeSize")
    private Integer codeSize; // 单个微服务模块总代码行数
    @ExcelProperty("entityNumber")
    private Integer entityNumber; // 单个微服务模块实体类数量
    @ExcelProperty("entityAttributeNumber")
    private Integer entityAttributeNumber; // 单个微服务模块实体类属性总数量
    @ExcelProperty("averageEntityAttribute")
    private Double averageEntityAttribute; // 单个微服务模块实体类平均属性数量
    @ExcelProperty("controllerNumber")
    private Integer controllerNumber; // 单个微服务模块控制器类数量
    @ExcelProperty("interfaceNumber")
    private Integer interfaceNumber; // 单个微服务模块接口数量
    @ExcelProperty("abstractClassNumber")
    private Integer abstractClassNumber; // 单个微服务模块抽象类数量
    @ExcelProperty("serviceClassNumber")
    private Integer serviceClassNumber; // 单个微服务模块业务类数量
    @ExcelProperty("dtoObjectNumber")
    private Integer dtoObjectNumber; // 单个微服务模块数据传输类数量
    @ExcelProperty("apiNumber")
    private Integer apiNumber; // 单个微服务模块 API 数量
    @ExcelProperty("apiVersionNumber")
    private Integer apiVersionNumber; // 单个微服务模块 API 版本数量
    @ExcelProperty("databaseNumber")
    private Integer databaseNumber; // 单个微服务模块配置的数据库数量
    @ExcelProperty("serviceImplCall")
    private String serviceImplCall; // 控制器层对业务层的调用情况
    @ExcelProperty("microserviceCall")
    private String microserviceCall; // 当前微服务模块所调用的微服务情况
    @ExcelProperty("microserviceCalled")
    private String microserviceCalled; // 调用当前微服务模块的微服务情况


}
