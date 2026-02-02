package com.example.bolt.plugin.examples;

import com.example.bolt.plugin.api.PluginConfig;
import com.example.bolt.plugin.api.PluginContext;
import com.example.bolt.plugin.api.PluginResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EchoPlugin 测试类
 */
class EchoPluginTest {

    private EchoPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new EchoPlugin();

        PluginConfig config = PluginConfig.builder()
                .pluginId("echo-plugin")
                .version("1.0.0")
                .instanceId("test-instance-001")
                .instanceName("Test Echo")
                .properties(Map.of("greetingPrefix", "Hi"))
                .build();

        PluginContext context = PluginContext.builder()
                .executionId("test-exec-001")
                .build();

        plugin.initialize(config, context);
    }

    @AfterEach
    void tearDown() {
        plugin.destroy();
    }

    @Test
    void testGetPluginId() {
        assertEquals("echo-plugin", plugin.getPluginId());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", plugin.getVersion());
    }

    @Test
    void testEchoAction() {
        Map<String, Object> params = new HashMap<>();
        params.put("message", "Hello World");
        params.put("name", "Bolt");

        PluginContext context = PluginContext.builder()
                .executionId("test-001")
                .build();

        PluginResult result = plugin.execute("echo", params, context);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("Hi, Bolt!", data.get("greeting"));
        assertEquals("Hello World", data.get("echo"));
    }

    @Test
    void testPingAction() {
        PluginContext context = PluginContext.builder().build();
        PluginResult result = plugin.execute("ping", new HashMap<>(), context);

        assertTrue(result.isSuccess());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("pong", data.get("status"));
        assertEquals("echo-plugin", data.get("pluginId"));
    }

    @Test
    void testTimeAction() {
        PluginContext context = PluginContext.builder().build();
        PluginResult result = plugin.execute("time", new HashMap<>(), context);

        assertTrue(result.isSuccess());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("datetime"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testUnsupportedAction() {
        PluginContext context = PluginContext.builder().build();
        PluginResult result = plugin.execute("unsupported", new HashMap<>(), context);

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testGetSupportedActions() {
        String[] actions = plugin.getSupportedActions();
        assertTrue(actions.length > 0);
    }
}
