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
    │           └── api.io.bolt.plugin.Plugin
    └── test/                        # 测试代码
```

## 最新插件规范（v2.0）

### 核心接口规范

| 组件 | 说明 | 必需 |
|------|------|------|
| `Plugin` | 插件主接口 | ✔ |
| `PluginConfig` | 配置封装 | ✔ |
| `PluginContext` | 执行上下文 | ✔ |
| `PluginResult` | 统一结果返回 | ✔ |
| `PluginException` | 异常定义 | ✔ |
| `PluginType` | 插件类型枚举 | ✔ |

### 插件类型定义

```java
public enum PluginType {
    UTILITY,      // 工具类插件
    DATABASE,     // 数据库插件
    HTTP_CLIENT,  // HTTP客户端
    MESSAGE_QUEUE,// 消息队列
    CACHE,        // 缓存插件
    CUSTOM        // 自定义类型
}
```

### 生命周期规范

```
创建 → 初始化(initialize) → 执行(execute) → 销毁(destroy)
         ↓                      ↓
      加载配置               健康检查
      建立连接               权限验证
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

import io.bolt.plugin.AbstractPlugin;
import io.bolt.plugin.api.PluginContext;
import io.bolt.plugin.api.PluginResult;

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

import io.bolt.plugin.api.*;

import java.util.Map;

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

编辑 `src/main/resources/META-INF/services/api.io.bolt.plugin.Plugin` 文件，添加你的插件类：

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

Bolt 通过 `BoltInternalApi` 暴露插件调用接口给 JavaScript 脚本。在脚本节点中，可以通过 `$bolt` 或 `$plugin` 对象调用插件。

### 通过 $bolt 对象调用（底层API）

`BoltInternalApi` 通过 GraalVM 的 `@HostAccess.Export` 注解将以下方法暴露给 JavaScript：

#### 1. 同步调用 - `call(instanceId, action, params)`

```javascript
// 同步调用插件动作
const jsonResult = $bolt.call('my-instance-001', 'process', {
    input: 'hello world',
    count: 5
});

// 解析 JSON 结果
const result = JSON.parse(jsonResult);
console.log(result.output);  // 访问返回数据
```

**参数说明：**
| 参数 | 类型 | 说明 |
|------|------|------|
| `instanceId` | string | 插件实例ID（在系统中创建实例时分配） |
| `action` | string | 要执行的动作名称 |
| `params` | object | 调用参数对象 |

**返回值：** JSON 字符串，需要 `JSON.parse()` 解析

#### 2. 异步调用 - `callAsync(instanceId, action, params)`

```javascript
// 异步调用（返回 Promise）
const promise = $bolt.callAsync('my-instance-001', 'fetchData', {
    url: 'https://api.example.com/data',
    timeout: 30000
});

// 使用 await（在 async 函数中）
const jsonResult = await $bolt.callAsync('my-instance-001', 'fetchData', params);
const result = JSON.parse(jsonResult);

// 或使用 Promise 链
$bolt.callAsync('my-instance-001', 'fetchData', params)
    .then(json => JSON.parse(json))
    .then(data => console.log(data))
    .catch(err => console.error(err));
```

#### 3. 批量调用 - `callBatch(calls)`

```javascript
// 批量调用多个插件动作
const calls = [
    { instanceId: 'db-instance-001', action: 'query', params: { sql: 'SELECT * FROM users' } },
    { instanceId: 'http-instance-002', action: 'get', params: { url: '/api/users' } },
    { instanceId: 'cache-instance-003', action: 'get', params: { key: 'user:123' } }
];

const jsonResults = $bolt.callBatch(calls);
const results = JSON.parse(jsonResults);
// results = [queryResult, httpResult, cacheResult]
```

#### 4. 检查插件可用性 - `isAvailable(instanceId)`

```javascript
// 检查插件实例是否已加载并可用
if ($bolt.isAvailable('my-instance-001')) {
    const result = $bolt.call('my-instance-001', 'process', { data: 'test' });
} else {
    console.warn('插件实例未就绪');
}
```

#### 5. 获取统计信息 - `getStats()`

```javascript
// 获取插件管理器统计信息
const stats = $bolt.getStats();
console.log(stats.loadedInstances);  // 已加载实例数
console.log(stats.totalCalls);       // 总调用次数
```

### 通过 $plugin 对象调用（推荐方式）

`$plugin` 是 Bolt 提供的 JavaScript 辅助对象，封装了 `$bolt` 的 JSON 解析逻辑，使用更简洁：

```javascript
// $plugin.call() 自动解析 JSON 结果
const result = $plugin.call('my-instance-001', 'process', {
    input: 'hello world'
});
// result 已经是解析后的对象，无需 JSON.parse()

// $plugin.callAsync() 返回解析后的 Promise
const result = await $plugin.callAsync('my-instance-001', 'fetchData', {
    url: 'https://api.example.com'
});

// $plugin.callBatch() 返回解析后的数组
const results = $plugin.callBatch([
    { instanceId: 'instance-001', action: 'action1', params: {} },
    { instanceId: 'instance-002', action: 'action2', params: {} }
]);
```

### 完整的脚本节点示例

```javascript
/**
 * 数据处理脚本节点示例
 * 演示如何在 Bolt 工作流中调用插件
 */

// 1. 检查依赖插件是否可用
if (!$bolt.isAvailable('db-mysql-001')) {
    throw new Error('MySQL 插件实例未就绪');
}
if (!$bolt.isAvailable('http-client-001')) {
    throw new Error('HTTP 客户端插件未就绪');
}

// 2. 从数据库查询配置
const dbResult = JSON.parse($bolt.call('db-mysql-001', 'query', {
    sql: 'SELECT api_url, api_key FROM configs WHERE id = ?',
    params: [configId]
}));

if (!dbResult || dbResult.length === 0) {
    throw new Error('未找到配置');
}

const config = dbResult[0];

// 3. 异步调用外部 API
const apiResponse = await $plugin.callAsync('http-client-001', 'post', {
    url: config.api_url,
    headers: {
        'Authorization': 'Bearer ' + config.api_key,
        'Content-Type': 'application/json'
    },
    body: {
        data: input.data,  // 来自上游节点的输入
        timestamp: new Date().toISOString()
    },
    timeout: 30000
});

// 4. 将结果写入缓存（批量调用示例）
const cacheResults = $plugin.callBatch([
    {
        instanceId: 'redis-cache-001',
        action: 'set',
        params: {
            key: 'api:response:' + input.id,
            value: JSON.stringify(apiResponse),
            ttl: 3600
        }
    },
    {
        instanceId: 'db-mysql-001',
        action: 'execute',
        params: {
            sql: 'INSERT INTO logs (request_id, status) VALUES (?, ?)',
            params: [input.id, apiResponse.status]
        }
    }
]);

// 5. 返回处理结果给下游节点
return {
    success: true,
    data: apiResponse,
    cached: cacheResults[0] !== null,
    logged: cacheResults[1] !== null
};
```

### 错误处理

```javascript
try {
    const result = $plugin.call('my-instance', 'riskyAction', params);
} catch (error) {
    // 可能的错误类型：
    // - 权限错误：无权限使用该插件实例
    // - 插件错误：插件内部执行失败
    // - 序列化错误：JSON 解析/序列化失败
    // - 超时错误：插件执行超时
    
    return {
        success: false,
        error: error.message,
        fallback: 'default value'
    };
}
```

### 调用方式对比

| 方式 | 特点 | 适用场景 |
|------|------|----------|
| `$bolt.call()` | 返回 JSON 字符串 | 需要原始 JSON 时 |
| `$plugin.call()` | 自动解析为对象 | **推荐使用** |
| `$plugin.callAsync()` | 异步执行 | 长时间操作 |
| `$plugin.callBatch()` | 批量执行 | 多插件协同 |
| `$bolt.isAvailable()` | 检查状态 | 前置验证 |

### Java 端实现参考

Java 端的 `BoltInternalApi` 实现了以下核心逻辑：

1. **权限检查**：通过 `ResourceAuthService` 验证用户权限
2. **上下文传递**：自动创建或复用 `PluginContext`
3. **插件调用**：委托给 `PluginManager` 执行
4. **结果序列化**：使用 Jackson 将结果转为 JSON
5. **异常处理**：统一包装为运行时异常

关键代码路径：`server/src/main/java/com/example/bolt/workflow/script/bridge/BoltInternalApi.java`

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
