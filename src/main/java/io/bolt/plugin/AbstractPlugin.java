package io.bolt.plugin;

import io.bolt.plugin.api.*;
import io.bolt.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件抽象基类
 * 建议插件继承此类，简化开发
 */
public abstract class AbstractPlugin implements Plugin {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected PluginConfig config;
    protected PluginContext initContext;
    protected volatile boolean initialized = false;

    // 支持的动作注册表
    private final Map<String, ActionHandler> actionHandlers = new ConcurrentHashMap<>();

    @Override
    public String getPluginName() {
        return getPluginId();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getAuthor() {
        return "";
    }

    @Override
    public void initialize(PluginConfig config, PluginContext context) throws PluginException {
        this.config = config;
        this.initContext = context;

        try {
            // 子类初始化
            doInitialize();

            // 注册动作处理器
            registerActions();

            this.initialized = true;
            logger.info("插件 [{}] v{} 初始化成功", getPluginId(), getVersion());
        } catch (Exception e) {
            logger.error("插件 [{}] 初始化失败: {}", getPluginId(), e.getMessage());
            throw new PluginException("INIT_FAILED", "插件初始化失败", e);
        }
    }

    @Override
    public PluginResult execute(String action, Map<String, Object> params, PluginContext context) {
        if (!initialized) {
            return PluginResult.failure("PLUGIN_NOT_INITIALIZED", "插件未初始化");
        }

        ActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            return PluginResult.failure("ACTION_NOT_SUPPORTED",
                    "不支持的动作: " + action + ", 支持的动作: " + actionHandlers.keySet());
        }

        long startTime = System.currentTimeMillis();
        try {
            logger.debug("执行动作 [{}] 参数: {}", action, params);
            PluginResult result = handler.execute(params, context);
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            logger.error("动作 [{}] 执行失败: {}", action, e.getMessage());
            return PluginResult.failure("EXECUTION_FAILED", e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            doDestroy();
            actionHandlers.clear();
            initialized = false;
            logger.info("插件 [{}] 已销毁", getPluginId());
        } catch (Exception e) {
            logger.error("插件 [{}] 销毁时发生错误: {}", getPluginId(), e.getMessage());
        }
    }

    @Override
    public String[] getSupportedActions() {
        Set<String> actions = actionHandlers.keySet();
        return actions.toArray(new String[0]);
    }

    /**
     * 子类实现：初始化逻辑
     */
    protected abstract void doInitialize() throws Exception;

    /**
     * 子类实现：销毁逻辑
     */
    protected abstract void doDestroy() throws Exception;

    /**
     * 子类实现：注册动作处理器
     */
    protected abstract void registerActions();

    /**
     * 注册动作处理器
     * @param action 动作名称
     * @param handler 处理器
     */
    protected void registerAction(String action, ActionHandler handler) {
        actionHandlers.put(action, handler);
        logger.debug("注册动作处理器: {}", action);
    }

    /**
     * 获取配置属性
     */
    @SuppressWarnings("unchecked")
    protected <T> T getProperty(String key, T defaultValue) {
        if (config == null || config.getProperties() == null) {
            return defaultValue;
        }
        Object value = config.getProperties().get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 获取配置属性（必需）
     */
    @SuppressWarnings("unchecked")
    protected <T> T getRequiredProperty(String key) {
        if (config == null || config.getProperties() == null) {
            throw new PluginException("CONFIG_MISSING", "缺少必需配置: " + key);
        }
        Object value = config.getProperties().get(key);
        if (value == null) {
            throw new PluginException("CONFIG_MISSING", "缺少必需配置: " + key);
        }
        return (T) value;
    }

    /**
     * 动作处理器接口
     */
    @FunctionalInterface
    public interface ActionHandler {
        PluginResult execute(Map<String, Object> params, PluginContext context);
    }
}
