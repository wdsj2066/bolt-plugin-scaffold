package com.example.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 插件配置类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig {

    /** 插件ID */
    private String pluginId;

    /** 插件版本 */
    private String version;

    /** 实例ID */
    private String instanceId;

    /** 实例名称 */
    private String instanceName;

    /** 配置属性 */
    private Map<String, Object> properties;

    /** 超时时间（毫秒） */
    private Long timeoutMs;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大并发数 */
    private Integer maxConcurrent;
}
