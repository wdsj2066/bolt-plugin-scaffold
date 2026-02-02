#!/bin/bash

# Bolt 插件快速创建脚本

PLUGIN_NAME=$1
PLUGIN_CLASS=$2

if [ -z "$PLUGIN_NAME" ] || [ -z "$PLUGIN_CLASS" ]; then
    echo "用法: ./create-plugin.sh <插件名称> <类名>"
    echo "示例: ./create-plugin.sh my-plugin MyPlugin"
    exit 1
fi

PACKAGE_DIR="src/main/java/com/example/bolt/plugin/custom"
mkdir -p "$PACKAGE_DIR"

CLASS_FILE="$PACKAGE_DIR/${PLUGIN_CLASS}.java"

cat > "$CLASS_FILE" << EOF
package com.example.bolt.plugin.custom;

import com.example.bolt.plugin.AbstractPlugin;
import com.example.bolt.plugin.api.PluginContext;
import com.example.bolt.plugin.api.PluginResult;

import java.util.HashMap;
import java.util.Map;

/**
 * ${PLUGIN_NAME} 插件
 */
public class ${PLUGIN_CLASS} extends AbstractPlugin {

    private static final String PLUGIN_ID = "${PLUGIN_NAME}";
    private static final String VERSION = "1.0.0";

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    protected void doInitialize() {
        // TODO: 初始化逻辑
        logger.info("${PLUGIN_CLASS} 初始化完成");
    }

    @Override
    protected void doDestroy() {
        // TODO: 清理资源
        logger.info("${PLUGIN_CLASS} 已销毁");
    }

    @Override
    protected void registerActions() {
        registerAction("process", this::doProcess);
        registerAction("status", this::doStatus);
    }

    /**
     * 处理动作
     */
    private PluginResult doProcess(Map<String, Object> params, PluginContext context) {
        // TODO: 实现业务逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "处理完成");

        return PluginResult.success(result);
    }

    /**
     * 状态检查
     */
    private PluginResult doStatus(Map<String, Object> params, PluginContext context) {
        Map<String, Object> result = new HashMap<>();
        result.put("pluginId", getPluginId());
        result.put("version", getVersion());
        result.put("healthy", true);

        return PluginResult.success(result);
    }
}
EOF

echo "✅ 插件类已创建: $CLASS_FILE"
echo ""
echo "下一步:"
echo "  1. 编辑 $CLASS_FILE 实现你的业务逻辑"
echo "  2. 在 src/main/resources/META-INF/services/com.example.bolt.plugin.api.Plugin 中注册:"
echo "     com.example.bolt.plugin.custom.${PLUGIN_CLASS}"
echo "  3. 运行 ./build.sh 打包"
