package com.example.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 插件上下文
 * 提供插件执行时的上下文信息
 *
 * @author Bolt
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginContext {

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 工作流实例ID
     */
    private String workflowInstanceId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 租户ID(多租户支持)
     */
    private String tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 创建默认上下文
     *
     * @return 插件上下文
     */
    public static PluginContext create() {
        return PluginContext.builder()
                .executionId(UUID.randomUUID().toString())
                .requestId(UUID.randomUUID().toString())
                .traceId(UUID.randomUUID().toString())
                .attributes(new HashMap<>())
                .build();
    }

    /**
     * 创建工作流节点上下文
     *
     * @param workflowInstanceId 工作流实例ID
     * @param nodeId             节点ID
     * @return 插件上下文
     */
    public static PluginContext forWorkflowNode(String workflowInstanceId, String nodeId) {
        return PluginContext.builder()
                .executionId(UUID.randomUUID().toString())
                .workflowInstanceId(workflowInstanceId)
                .nodeId(nodeId)
                .requestId(UUID.randomUUID().toString())
                .traceId(UUID.randomUUID().toString())
                .attributes(new HashMap<>())
                .build();
    }

    /**
     * 设置属性
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    /**
     * 获取属性
     *
     * @param key 属性键
     * @return 属性值
     */
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    /**
     * 获取属性(带默认值)
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = getAttribute(key);
        return value != null ? (T) value : defaultValue;
    }
}
