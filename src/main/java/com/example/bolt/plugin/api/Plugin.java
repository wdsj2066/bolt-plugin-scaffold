package com.example.bolt.plugin.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Bolt 插件核心接口
 * 所有插件必须实现此接口
 */
public interface Plugin {

    /**
     * 获取插件唯一标识
     * @return 插件ID，全局唯一
     */
    String getPluginId();

    /**
     * 获取插件版本
     * @return 版本号，如 "1.0.0"
     */
    String getVersion();

    /**
     * 获取插件类型
     * @return 插件类型，默认为CUSTOM
     */
    default PluginType getPluginType() {
        return PluginType.CUSTOM;
    }

    /**
     * 获取插件名称
     * @return 插件显示名称，默认返回插件ID
     */
    default String getPluginName() {
        return getPluginId();
    }

    /**
     * 获取插件描述
     * @return 插件描述，默认返回空字符串
     */
    default String getDescription() {
        return "";
    }

    /**
     * 获取插件作者
     * @return 插件作者，默认返回空字符串
     */
    default String getAuthor() {
        return "";
    }

    /**
     * 初始化插件
     * @param config 插件配置
     * @param context 插件上下文
     * @throws PluginException 初始化失败时抛出
     */
    void initialize(PluginConfig config, PluginContext context) throws PluginException;

    /**
     * 异步执行插件动作
     * @param action 动作名称
     * @param params 执行参数
     * @param context 执行上下文
     * @return 异步执行结果
     */
    default CompletableFuture<PluginResult> executeAsync(String action,
            Map<String, Object> params, PluginContext context) {
        return CompletableFuture.supplyAsync(() -> execute(action, params, context));
    }

    /**
     * 同步执行插件动作
     * @param action 动作名称
     * @param params 执行参数
     * @param context 执行上下文
     * @return 执行结果
     */
    PluginResult execute(String action, Map<String, Object> params, PluginContext context);

    /**
     * 销毁插件，释放资源
     */
    void destroy();

    /**
     * 获取支持的动作列表
     * @return 动作名称数组
     */
    default String[] getSupportedActions() {
        return new String[0];
    }

    /**
     * 获取健康状态
     * @return 健康状态
     */
    default PluginHealthStatus getHealthStatus() {
        return PluginHealthStatus.healthy();
    }
}
