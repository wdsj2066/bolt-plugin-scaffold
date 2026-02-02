package com.example.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件执行结果
 *
 * @author Bolt
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 返回数据
     */
    private Object data;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 错误编码
     */
    private String errorCode;

    /**
     * 执行耗时(毫秒)
     */
    private Long executionTimeMs;

    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 创建成功结果
     *
     * @param data 返回数据
     * @return 执行结果
     */
    public static PluginResult success(Object data) {
        return PluginResult.builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 创建成功结果
     *
     * @return 执行结果
     */
    public static PluginResult success() {
        return success(null);
    }

    /**
     * 创建失败结果
     *
     * @param error 错误信息
     * @return 执行结果
     */
    public static PluginResult failure(String error) {
        return PluginResult.builder()
                .success(false)
                .error(error)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误编码
     * @param error     错误信息
     * @return 执行结果
     */
    public static PluginResult failure(String errorCode, String error) {
        return PluginResult.builder()
                .success(false)
                .errorCode(errorCode)
                .error(error)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param throwable 异常
     * @return 执行结果
     */
    public static PluginResult failure(Throwable throwable) {
        return PluginResult.builder()
                .success(false)
                .error(throwable.getMessage())
                .errorCode(throwable.getClass().getSimpleName())
                .build();
    }

    /**
     * 添加元数据
     *
     * @param key   键
     * @param value 值
     * @return 当前对象(链式调用)
     */
    public PluginResult withMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
        return this;
    }

    /**
     * 获取数据(Map类型)
     *
     * @return Map类型的数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDataAsMap() {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }
}
