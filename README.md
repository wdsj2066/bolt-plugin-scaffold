# Bolt 插件开发脚手架

这是一个独立的 Bolt 插件开发脚手架，帮助你快速创建和打包 Bolt 插件。

## 目录结构

```
plugin-scaffold/
├── pom.xml                          # Maven 构建配置
├── README.md                        # 本文件
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/bolt/plugin/
    │   │       ├── api/             # 插件 API 接口（核心）
    │   │       │   ├── Plugin.java           # 插件接口
    │   │       │   ├── PluginConfig.java     # 配置类
    │   │       │   ├── PluginContext.java    # 上下文
    │   │       │   ├── PluginResult.java     # 结果类
    │   │       │   ├── PluginException.java  # 异常类
    │   │       │   ├── PluginHealthStatus.java # 健康状态
    │   │       │   └── PluginType.java       # 类型枚举
    │   │       ├── AbstractPlugin.java       # 抽象基类
    │   │       └── examples/        # 示例插件
    │   │           ├── EchoPlugin.java       # 回显插件
    │   │           ├── HttpClientPlugin.java # HTTP客户端
    │   │           └── DatabasePlugin.java   # 数据库插件
    │   └── resources/
    │       └── META-INF/services/   # SPI 服务注册
    │           └── com.example.bolt.plugin.api.Plugin
    └── test/                        # 测试代码
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+

### 2. 构建项目

```bash
cd plugin-scaffold
mvn clean package
```

构建成功后，`target/` 目录下会生成：
- `bolt-plugin-scaffold-1.0.0.jar` - 插件 JAR 包

### 3. 开发自定义插件

#### 方式一：继承 AbstractPlugin（推荐）

```java
package com.example.bolt.plugin;

import com.example.bolt.plugin.api.*;
import java.util.Map;

public class MyPlugin extends AbstractPlugin {

    @Override
    public String getPluginId() {
        return "my-plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    protected void doInitialize() {
        // 初始化逻辑
        String apiKey = getProperty("apiKey", "default-key");
        logger.info("MyPlugin 初始化，API Key: {}", apiKey);
    }

    @Override
    protected void doDestroy() {
        // 清理资源
        logger.info("MyPlugin 销毁");
    }

    @Override
    protected void registerActions() {
        registerAction("process", this::doProcess);
        registerAction("status", this::doStatus);
    }

    private PluginResult doProcess(Map<String, Object> params, PluginContext context) {
        String input = (String) params.get("input");

        // 业务逻辑
        String output = input.toUpperCase();

        return PluginResult.success(Map.of(
            "output", output,
            "timestamp", System.currentTimeMillis()
        ));
    }

    private PluginResult doStatus(Map<String, Object> params, PluginContext context) {
        return PluginResult.success(Map.of(
            "status", "running",
            "pluginId", getPluginId()
        ));
    }
}
```

#### 方式二：实现 Plugin 接口

```java
package com.example.bolt.plugin;

import com.example.bolt.plugin.api.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MyRawPlugin implements Plugin {

    @Override
    public String getPluginId() {
        return "my-raw-plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void initialize(PluginConfig config, PluginContext context) throws PluginException {
        // 初始化
    }

    @Override
    public PluginResult execute(String action, Map<String, Object> params, PluginContext context) {
        switch (action) {
            case "process":
                return PluginResult.success("processed");
            default:
                return PluginResult.failure("UNKNOWN_ACTION", "未知动作");
        }
    }

    @Override
    public void destroy() {
        // 清理
    }
}
```

### 4. 注册插件

编辑 `src/main/resources/META-INF/services/com.example.bolt.plugin.api.Plugin` 文件，添加你的插件类：

```
com.example.bolt.plugin.MyPlugin
```

### 5. 打包并部署

```bash
mvn clean package
```

将生成的 JAR 文件上传到 Bolt 系统的插件管理界面。

## API 说明

### Plugin 接口

| 方法 | 说明 |
|------|------|
| `getPluginId()` | 返回插件唯一标识 |
| `getVersion()` | 返回插件版本 |
| `initialize(config, context)` | 初始化插件 |
| `execute(action, params, context)` | 同步执行动作 |
| `executeAsync(action, params, context)` | 异步执行动作（可选） |
| `destroy()` | 销毁插件，释放资源 |
| `getSupportedActions()` | 返回支持的动作列表 |
| `getHealthStatus()` | 返回健康状态 |

### AbstractPlugin 辅助方法

| 方法 | 说明 |
|------|------|
| `registerAction(name, handler)` | 注册动作处理器 |
| `getProperty(key, defaultValue)` | 获取配置属性 |
| `getRequiredProperty(key)` | 获取必需配置属性 |

## 示例插件说明

### EchoPlugin - 回显插件

最简单的示例，演示基本结构。

**支持的动作：**
- `echo` - 回显消息
- `ping` - 健康检查
- `time` - 获取时间
- `info` - 获取插件信息

### HttpClientPlugin - HTTP 客户端

演示如何调用外部 HTTP API。

**支持的动作：**
- `get` - GET 请求
- `post` - POST 请求
- `put` - PUT 请求
- `delete` - DELETE 请求
- `request` - 通用请求

**配置示例：**
```json
{
    "defaultTimeout": 30000,
    "defaultRetryCount": 3
}
```

### DatabasePlugin - 数据库插件

演示数据库操作。

**支持的动作：**
- `query` - 查询
- `execute` - 执行 DDL
- `update` - 更新
- `batch` - 批量操作
- `test` - 连接测试

**配置示例：**
```json
{
    "jdbcUrl": "jdbc:mysql://localhost:3306/mydb",
    "username": "root",
    "password": "password"
}
```

## 在 JavaScript 中调用插件

```javascript
// 同步调用
const result = $bolt.call('my-instance-001', 'process', {
    input: 'hello world'
});

// 异步调用
const promise = $bolt.callAsync('my-instance-001', 'process', {
    input: 'hello world'
});

// 检查插件可用性
const available = $bolt.isAvailable('my-instance-001');
```

## 配置参数 Schema

插件可以定义配置参数的 JSON Schema，用于系统 UI 生成配置表单：

```java
@Override
default String getConfigSchema() {
    return """
    {
        "type": "object",
        "properties": {
            "apiKey": {
                "type": "string",
                "title": "API Key",
                "description": "API 访问密钥"
            },
            "timeout": {
                "type": "integer",
                "title": "超时时间",
                "default": 30000
            }
        },
        "required": ["apiKey"]
    }
    """;
}
```

## 最佳实践

1. **使用抽象基类**：继承 `AbstractPlugin` 可以简化开发
2. **参数校验**：对输入参数进行有效性检查
3. **错误处理**：返回有意义的错误信息
4. **日志记录**：使用 `logger` 记录关键操作
5. **资源管理**：在 `destroy()` 中释放资源
6. **线程安全**：确保插件在多线程环境下安全

## 测试

```bash
# 运行所有测试
mvn test

# 运行带网络访问的测试（需要 ENABLE_NETWORK_TESTS=true）
ENABLE_NETWORK_TESTS=true mvn test
```

## 打包说明

项目使用 `maven-shade-plugin` 打包，会自动包含所有依赖。注意：

- Bolt 系统核心类（Plugin 接口等）**不应**打包进 JAR
- 插件所需的第三方依赖**应该**打包进 JAR
- 本脚手架已将 `api` 包设置为 provided scope，不会被打包

## 许可证

MIT License
