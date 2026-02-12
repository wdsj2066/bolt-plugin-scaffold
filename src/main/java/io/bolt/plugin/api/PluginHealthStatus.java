package io.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件健康
 *
 * @author Bolt
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginHealthStatus {

    /**
     * 状态 HEALTHY, UNHEALTHY, DEGRADED, UNKNOWN
     */
    private String status;

    /**
     * 状态描述
     */
    private String message;

    /**
     * 健康检查详情
     */
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    /**
     * 创建健康状态
     *
     * @return 健康状态
     */
    public static PluginHealthStatus healthy() {
        return PluginHealthStatus.builder()
                .status("HEALTHY")
                .message("插件运行正常")
                .build();
    }

    /**
     * 创建健康状态
     *
     * @param message 状态描述
     * @return 健康状态
     */
    public static PluginHealthStatus healthy(String message) {
        return PluginHealthStatus.builder()
                .status("HEALTHY")
                .message(message)
                .build();
    }

    /**
     * 创建不健康状态
     *
     * @param message 状态描述
     * @return 健康状态
     */
    public static PluginHealthStatus unhealthy(String message) {
        return PluginHealthStatus.builder()
                .status("UNHEALTHY")
                .message(message)
                .build();
    }

    /**
     * 创建降级状态
     *
     * @param message 状态描述
     * @return 健康状态
     */
    public static PluginHealthStatus degraded(String message) {
        return PluginHealthStatus.builder()
                .status("DEGRADED")
                .message(message)
                .build();
    }

    /**
     * 创建未知状态
     *
     * @param message 状态描述
     * @return 健康状态
     */
    public static PluginHealthStatus unknown(String message) {
        return PluginHealthStatus.builder()
                .status("UNKNOWN")
                .message(message)
                .build();
    }

    /**
     * 检查是否健康
     *
     * @return 是否健康
     */
    public boolean isHealthy() {
        return "HEALTHY".equals(status);
    }

    /**
     * 添加详情
     *
     * @param key   键
     * @param value 值
     * @return 当前对象(链式调用)
     */
    public PluginHealthStatus withDetail(String key, Object value) {
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
        return this;
    }
}
