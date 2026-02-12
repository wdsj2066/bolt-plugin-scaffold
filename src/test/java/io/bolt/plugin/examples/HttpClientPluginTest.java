package io.bolt.plugin.examples;

import io.bolt.plugin.api.PluginConfig;
import io.bolt.plugin.api.PluginContext;
import io.bolt.plugin.api.PluginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpClientPlugin 测试类
 * 需要网络连接
 */
@EnabledIfEnvironmentVariable(named = "ENABLE_NETWORK_TESTS", matches = "true")
class HttpClientPluginTest {

    private HttpClientPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new HttpClientPlugin();

        PluginConfig config = PluginConfig.builder()
                .pluginId("http-client-plugin")
                .version("1.0.0")
                .instanceId("http-test-001")
                .instanceName("Test HTTP Client")
                .properties(new HashMap<>())
                .build();

        plugin.initialize(config, PluginContext.builder().build());
    }

    @Test
    void testGetRequest() {
        Map<String, Object> params = new HashMap<>();
        params.put("url", "https://httpbin.org/get");
        params.put("timeout", 10000);

        PluginResult result = plugin.execute("get", params, PluginContext.builder().build());

        assertTrue(result.isSuccess(), "请求失败: " + result.getError());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("statusCode"));
        assertNotNull(data.get("body"));
    }

    @Test
    void testPostRequest() {
        Map<String, Object> params = new HashMap<>();
        params.put("url", "https://httpbin.org/post");
        params.put("body", "{\"name\":\"test\"}");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        params.put("headers", headers);

        PluginResult result = plugin.execute("post", params, PluginContext.builder().build());

        assertTrue(result.isSuccess(), "请求失败: " + result.getError());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(200, data.get("statusCode"));
    }

    @Test
    void testMissingUrlParam() {
        Map<String, Object> params = new HashMap<>();
        // 不传递 url 参数

        PluginResult result = plugin.execute("get", params, PluginContext.builder().build());

        assertFalse(result.isSuccess());
    }
}
