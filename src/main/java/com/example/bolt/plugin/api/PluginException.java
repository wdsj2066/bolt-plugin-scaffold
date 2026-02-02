package com.example.bolt.plugin.api;

/**
 * 插件异常
 * 插件相关操作抛出的异常
 *
 * @author Bolt
 * @since 2026-02-03
 */
public class PluginException extends RuntimeException {

    /**
     * 错误编码
     */
    private final String errorCode;

    /**
     * 插件ID
     */
    private final String pluginId;

    /**
     * 构造方法
     *
     * @param message 错误信息
     */
    public PluginException(String message) {
        super(message);
        this.errorCode = null;
        this.pluginId = null;
    }

    /**
     * 构造方法
     *
     * @param message   错误信息
     * @param errorCode 错误编码
     */
    public PluginException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.pluginId = null;
    }

    /**
     * 构造方法
     *
     * @param message 错误信息
     * @param cause   异常原因
     */
    public PluginException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.pluginId = null;
    }

    /**
     * 构造方法
     *
     * @param message   错误信息
     * @param errorCode 错误编码
     * @param cause     异常原因
     */
    public PluginException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.pluginId = null;
    }



    /**
     * 构造方法
     *
     * @param pluginId  插件ID
     * @param message   错误信息
     * @param errorCode 错误编码
     */
    public PluginException(String pluginId, String message, String errorCode) {
        super(message);
        this.pluginId = pluginId;
        this.errorCode = errorCode;
    }



    /**
     * 获取错误编码
     *
     * @return 错误编码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取插件ID
     *
     * @return 插件ID
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * 创建插件未找到异常
     *
     * @param pluginId 插件ID
     * @return 插件异常
     */
    public static PluginException notFound(String pluginId) {
        return new PluginException(pluginId, "插件未找到: " + pluginId, "PLUGIN_NOT_FOUND");
    }

    /**
     * 创建动作不支持异常
     *
     * @param pluginId 插件ID
     * @param action   动作名称
     * @return 插件异常
     */
    public static PluginException actionNotSupported(String pluginId, String action) {
        return new PluginException(pluginId, "插件不支持动作: " + action, "ACTION_NOT_SUPPORTED");
    }

    /**
     * 创建初始化失败异常
     *
     * @param pluginId 插件ID
     * @param cause    异常原因
     * @return 插件异常
     */
    public static PluginException initializationFailed(String pluginId, Throwable cause) {
        return new PluginException(pluginId, "插件初始化失败", cause);
    }

    /**
     * 创建执行失败异常
     *
     * @param pluginId 插件ID
     * @param cause    异常原因
     * @return 插件异常
     */
    public static PluginException executionFailed(String pluginId, Throwable cause) {
        return new PluginException(pluginId, "插件执行失败", cause);
    }

    /**
     * 创建资源配额超限异常
     *
     * @param pluginId 插件ID
     * @return 插件异常
     */
    public static PluginException quotaExceeded(String pluginId) {
        return new PluginException(pluginId, "插件资源配额超限", "QUOTA_EXCEEDED");
    }

    /**
     * 创建超时异常
     *
     * @param pluginId 插件ID
     * @param timeoutMs 超时时间(毫秒)
     * @return 插件异常
     */
    public static PluginException timeout(String pluginId, long timeoutMs) {
        return new PluginException(pluginId, "插件执行超时: " + timeoutMs + "ms", "EXECUTION_TIMEOUT");
    }
}
