@echo off
chcp 65001 >nul

:: Bolt 插件快速创建脚本

set PLUGIN_NAME=%1
set PLUGIN_CLASS=%2

if "%PLUGIN_NAME%"=="" (
    echo 用法: create-plugin.bat ^<插件名称^> ^<类名^>
    echo 示例: create-plugin.bat my-plugin MyPlugin
    exit /b 1
)

set PACKAGE_DIR=src\main\java\com\example\bolt\plugin\custom
if not exist "%PACKAGE_DIR%" mkdir "%PACKAGE_DIR%"

set CLASS_FILE=%PACKAGE_DIR%\%PLUGIN_CLASS%.java

echo package com.example.bolt.plugin.custom;> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo import com.example.bolt.plugin.AbstractPlugin;>> "%CLASS_FILE%"
echo import com.example.bolt.plugin.api.PluginContext;>> "%CLASS_FILE%"
echo import com.example.bolt.plugin.api.PluginResult;>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo import java.util.HashMap;>> "%CLASS_FILE%"
echo import java.util.Map;>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo /**>> "%CLASS_FILE%"
echo  * %PLUGIN_NAME% 插件>> "%CLASS_FILE%"
echo  */>> "%CLASS_FILE%"
echo public class %PLUGIN_CLASS% extends AbstractPlugin {>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     private static final String PLUGIN_ID = "%PLUGIN_NAME%";>> "%CLASS_FILE%"
echo     private static final String VERSION = "1.0.0";>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     @Override>> "%CLASS_FILE%"
echo     public String getPluginId() {>> "%CLASS_FILE%"
echo         return PLUGIN_ID;>> "%CLASS_FILE%"
echo     }>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     @Override>> "%CLASS_FILE%"
echo     public String getVersion() {>> "%CLASS_FILE%"
echo         return VERSION;>> "%CLASS_FILE%"
echo     }>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     @Override>> "%CLASS_FILE%"
echo     protected void doInitialize() {>> "%CLASS_FILE%"
echo         // TODO: 初始化逻辑>> "%CLASS_FILE%"
echo         logger.info("%PLUGIN_CLASS% 初始化完成");>> "%CLASS_FILE%"
echo     }>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     @Override>> "%CLASS_FILE%"
echo     protected void doDestroy() {>> "%CLASS_FILE%"
echo         // TODO: 清理资源>> "%CLASS_FILE%"
echo         logger.info("%PLUGIN_CLASS% 已销毁");>> "%CLASS_FILE%"
echo     }>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     @Override>> "%CLASS_FILE%"
echo     protected void registerActions() {>> "%CLASS_FILE%"
echo         registerAction("process", this::doProcess);>> "%CLASS_FILE%"
echo         registerAction("status", this::doStatus);>> "%CLASS_FILE%"
echo     }>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     /**>> "%CLASS_FILE%"
echo      * 处理动作>> "%CLASS_FILE%"
echo      */>> "%CLASS_FILE%"
echo     private PluginResult doProcess(Map^<String, Object^> params, PluginContext context) {>> "%CLASS_FILE%"
echo         // TODO: 实现业务逻辑>> "%CLASS_FILE%"
echo         Map^<String, Object^> result = new HashMap^<>();>> "%CLASS_FILE%"
echo         result.put("status", "success");>> "%CLASS_FILE%"
echo         result.put("message", "处理完成");>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo         return PluginResult.success(result);>> "%CLASS_FILE%"
echo     }>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo     /**>> "%CLASS_FILE%"
echo      * 状态检查>> "%CLASS_FILE%"
echo      */>> "%CLASS_FILE%"
echo     private PluginResult doStatus(Map^<String, Object^> params, PluginContext context) {>> "%CLASS_FILE%"
echo         Map^<String, Object^> result = new HashMap^<>();>> "%CLASS_FILE%"
echo         result.put("pluginId", getPluginId());>> "%CLASS_FILE%"
echo         result.put("version", getVersion());>> "%CLASS_FILE%"
echo         result.put("healthy", true);>> "%CLASS_FILE%"
echo.>> "%CLASS_FILE%"
echo         return PluginResult.success(result);>> "%CLASS_FILE%"
echo     }>> "%CLASS_FILE%"
echo }>> "%CLASS_FILE%"

echo ✅ 插件类已创建: %CLASS_FILE%
echo.
echo 下一步:
echo   1. 编辑 %CLASS_FILE% 实现你的业务逻辑
echo   2. 在 src\main\resources\META-INF\services\com.example.bolt.plugin.api.Plugin 中注册:
echo      com.example.bolt.plugin.custom.%PLUGIN_CLASS%
echo   3. 运行 build.bat 打包
