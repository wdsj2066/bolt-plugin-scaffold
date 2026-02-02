package com.example.bolt.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件执行上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginContext {

    /** 执行ID */
    private String executionId;

    /** 工作流实例ID */
    private String workflowInstanceId;

    /** 节点ID */
    private String nodeId;

    /** 租户ID */
    private String tenantId;

    /** 用户ID */
    private Long userId;

    /** 请求ID */
    private String requestId;

    /** 追踪ID */
    private String traceId;

    /** 扩展属性 */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 创建工作流节点上下文
     */
    public static PluginContext forWorkflowNode(String workflowInstanceId, String nodeId) {
        return PluginContext.builder()
                .workflowInstanceId(workflowInstanceId)
                .nodeId(nodeId)
                .build();
    }

    /**
     * 获取属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 设置属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
