package io.bolt.plugin.examples;

import io.bolt.plugin.AbstractPlugin;
import io.bolt.plugin.api.PluginContext;
import io.bolt.plugin.api.PluginResult;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Echo 示例插件
 * 最简单的插件示例，用于测试和学习
 */
public class EchoPlugin extends AbstractPlugin {

    private static final String PLUGIN_ID = "echo-plugin";
    private static final String VERSION = "1.0.0";

    private String greetingPrefix;

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getPluginName() {
        return "Echo 插件";
    }

    @Override
    public String getDescription() {
        return "一个简单的回显插件，用于测试和学习插件系统";
    }

    @Override
    public String getAuthor() {
        return "Bolt Team";
    }

    @Override
    public String getPluginType() {
        return "CUSTOM";
    }

    @Override
    protected void doInitialize() {
        // 从配置读取问候前缀，默认为 "Hello"
        this.greetingPrefix = getProperty("greetingPrefix", "Hello");
        logger.info("EchoPlugin 初始化完成，问候前缀: {}", greetingPrefix);
    }

    @Override
    protected void doDestroy() {
        // 清理资源
        logger.info("EchoPlugin 已销毁");
    }

    @Override
    protected void registerActions() {
        registerAction("echo", this::doEcho);
        registerAction("ping", this::doPing);
        registerAction("time", this::doTime);
        registerAction("info", this::doInfo);
    }

    /**
     * Echo 动作 - 回显输入内容
     */
    private PluginResult doEcho(Map<String, Object> params, PluginContext context) {
        String message = (String) params.getOrDefault("message", "");
        String name = (String) params.getOrDefault("name", "World");

        Map<String, Object> result = new HashMap<>();
        result.put("greeting", greetingPrefix + ", " + name + "!");
        result.put("echo", message);
        result.put("timestamp", System.currentTimeMillis());
        result.put("executionId", context.getExecutionId());

        return PluginResult.success(result);
    }

    /**
     * Ping 动作 - 健康检查
     */
    private PluginResult doPing(Map<String, Object> params, PluginContext context) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "pong");
        result.put("pluginId", getPluginId());
        result.put("version", getVersion());
        result.put("instanceId", config.getInstanceId());

        return PluginResult.success(result);
    }

    /**
     * Time 动作 - 获取当前时间
     */
    private PluginResult doTime(Map<String, Object> params, PluginContext context) {
        String format = (String) params.getOrDefault("format", "yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        Map<String, Object> result = new HashMap<>();
        result.put("datetime", LocalDateTime.now().format(formatter));
        result.put("timestamp", System.currentTimeMillis());
        result.put("timezone", java.time.ZoneId.systemDefault().getId());

        return PluginResult.success(result);
    }

    /**
     * Info 动作 - 获取插件信息
     */
    private PluginResult doInfo(Map<String, Object> params, PluginContext context) {
        Map<String, Object> result = new HashMap<>();
        result.put("pluginId", getPluginId());
        result.put("version", getVersion());
        result.put("instanceId", config.getInstanceId());
        result.put("instanceName", config.getInstanceName());
        result.put("supportedActions", getSupportedActions());
        result.put("properties", config.getProperties());

        return PluginResult.success(result);
    }
}
