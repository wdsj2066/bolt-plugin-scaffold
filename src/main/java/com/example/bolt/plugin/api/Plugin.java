package com.example.bolt.plugin.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 插件核心接口
 * 所有插件必须实现此接口
 *
 * @author Bolt
 * @since 2026-02-03
 */
public interface Plugin {

    /**
     * 获取插件唯一标识
     *
     * @return 插件ID
     */
    String getPluginId();

    /**
     * 获取插件版本号
     *
     * @return 版本号
     */
    String getVersion();

    /**
     * 获取插件名称
     *
     * @return 插件名称
     */
    default String getPluginName() {
        return getPluginId();
    }

    /**
     * 获取插件类型
     * 返回值可以是: DATASOURCE, HARDWARE, SECURITY, NOTIFICATION, STORAGE, AI, CUSTOM
     *
     * @return 插件类型
     */
    default String getPluginType() {
        return "CUSTOM";
    }

    String getDescription();

    String getAuthor();

    /**
     * 初始化插件
     * 在插件加载时调用,用于初始化配置和系统上下文
     *
     * @param config  插件配置
     * @param context 插件上下文
     * @throws PluginException 初始化失败时抛出
     */
    void initialize(PluginConfig config, PluginContext context) throws PluginException;

    /**
     * 异步执行插件动作
     * 适配高延迟场景(IoT/外部API)
     *
     * @param action  要执行的动作/方法名
     * @param params  调用参数
     * @param context 插件上下文
     * @return 异步执行结果
     */
    default CompletableFuture<PluginResult> executeAsync(String action, Map<String, Object> params, PluginContext context) {
        return CompletableFuture.supplyAsync(() -> execute(action, params, context));
    }

    /**
     * 同步执行插件动作
     *
     * @param action  要执行的动作/方法名
     * @param params  调用参数
     * @param context 插件上下文
     * @return 执行结果
     */
    PluginResult execute(String action, Map<String, Object> params, PluginContext context);

    /**
     * 销毁插件
     * 在插件卸载时调用,用于释放资源
     */
    void destroy();

    /**
     * 获取插件支持的动作列表
     *
     * @return 支持的动作名称数组
     */
    default String[] getSupportedActions() {
        return new String[0];
    }

    /**
     * 检查是否支持指定动作
     *
     * @param action 动作名称
     * @return 是否支持
     */
    default boolean supportsAction(String action) {
        for (String supported : getSupportedActions()) {
            if (supported.equals(action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取插件健康状态
     *
     * @return 健康状态信息
     */
    default PluginHealthStatus getHealthStatus() {
        return PluginHealthStatus.healthy();
    }

    /**
     * 获取默认配置JSON
     * 返回插件的默认配置模板，用于创建实例时自动填充
     *
     * @return 默认配置JSON字符串
     */
    default String getDefaultConfigJson() {
        return "{}";
    }
}
