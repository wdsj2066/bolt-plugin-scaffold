package com.example.bolt.plugin.api;

/**
 * 插件异常类
 */
public class PluginException extends RuntimeException {

    private final String errorCode;

    public PluginException(String message) {
        super(message);
        this.errorCode = "PLUGIN_ERROR";
    }

    public PluginException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PLUGIN_ERROR";
    }

    public PluginException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 配额超限异常
     */
    public static PluginException quotaExceeded(String pluginId) {
        return new PluginException("QUOTA_EXCEEDED",
                "插件 [" + pluginId + "] 并发请求超限");
    }

    /**
     * 动作不支持异常
     */
    public static PluginException actionNotSupported(String action) {
        return new PluginException("ACTION_NOT_SUPPORTED",
                "不支持的动作: " + action);
    }

    /**
     * 参数无效异常
     */
    public static PluginException invalidParam(String paramName) {
        return new PluginException("INVALID_PARAM",
                "无效参数: " + paramName);
    }

    /**
     * 执行超时异常
     */
    public static PluginException timeout(String action) {
        return new PluginException("EXECUTION_TIMEOUT",
                "动作 [" + action + "] 执行超时");
    }
}
