package com.example.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 插件执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginResult {

    /** 是否成功 */
    private boolean success;

    /** 返回数据 */
    private Object data;

    /** 错误信息 */
    private String error;

    /** 错误编码 */
    private String errorCode;

    /** 执行耗时（毫秒） */
    private Long executionTimeMs;

    /** 元数据 */
    private Map<String, Object> metadata;

    /**
     * 创建成功结果
     */
    public static PluginResult success(Object data) {
        return PluginResult.builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 创建成功结果（带元数据）
     */
    public static PluginResult success(Object data, Map<String, Object> metadata) {
        return PluginResult.builder()
                .success(true)
                .data(data)
                .metadata(metadata)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static PluginResult failure(String error) {
        return PluginResult.builder()
                .success(false)
                .error(error)
                .build();
    }

    /**
     * 创建失败结果（带错误码）
     */
    public static PluginResult failure(String errorCode, String error) {
        return PluginResult.builder()
                .success(false)
                .errorCode(errorCode)
                .error(error)
                .build();
    }
}
