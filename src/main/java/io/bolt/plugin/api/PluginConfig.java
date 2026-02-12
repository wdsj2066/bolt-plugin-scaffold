package io.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 插件配置
 * 包含插件的配置参数和元数据
 *
 * @author Bolt
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig {

    /**
     * 插件ID
     */
    private String pluginId;

    /**
     * 插件版本
     */
    private String version;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 配置参数
     */
    private Map<String, Object> properties;

    /**
     * 超时时间(毫秒)
     */
    private Long timeoutMs;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 重试间隔(毫秒)
     */
    private Long retryIntervalMs;

    /**
     * 最大并发数
     */
    private Integer maxConcurrent;

    /**
     * 获取配置参数
     *
     * @param key 参数键
     * @return 参数值
     */
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    /**
     * 获取字符串类型的配置参数
     *
     * @param key 参数键
     * @return 参数值
     */
    public String getStringProperty(String key) {
        Object value = getProperty(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取整数类型的配置参数
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    public int getIntProperty(String key, int defaultValue) {
        Object value = getProperty(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 获取布尔类型的配置参数
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        Object value = getProperty(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
}
