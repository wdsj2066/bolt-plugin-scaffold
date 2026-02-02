package com.example.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 插件健康状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginHealthStatus {

    /** 是否健康 */
    private boolean healthy;

    /** 状态信息 */
    private String message;

    /** 健康检查详情 */
    private Object details;

    /**
     * 创建健康状态
     */
    public static PluginHealthStatus healthy() {
        return new PluginHealthStatus(true, "OK", null);
    }

    /**
     * 创建健康状态（带详情）
     */
    public static PluginHealthStatus healthy(String message, Object details) {
        return new PluginHealthStatus(true, message, details);
    }

    /**
     * 创建不健康状态
     */
    public static PluginHealthStatus unhealthy(String message) {
        return new PluginHealthStatus(false, message, null);
    }

    /**
     * 创建不健康状态（带详情）
     */
    public static PluginHealthStatus unhealthy(String message, Object details) {
        return new PluginHealthStatus(false, message, details);
    }
}
