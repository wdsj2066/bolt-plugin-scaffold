package com.example.bolt.plugin.examples;

import com.example.bolt.plugin.AbstractPlugin;
import com.example.bolt.plugin.api.PluginContext;
import com.example.bolt.plugin.api.PluginException;
import com.example.bolt.plugin.api.PluginResult;
import com.example.bolt.plugin.api.PluginType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 客户端插件示例
 * 演示如何创建调用外部 HTTP API 的插件
 */
public class HttpClientPlugin extends AbstractPlugin {

    private static final String PLUGIN_ID = "http-client-plugin";
    private static final String VERSION = "1.0.0";

    private int defaultTimeout;
    private int defaultRetryCount;

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
        return "HTTP 客户端插件";
    }

    @Override
    public String getDescription() {
        return "提供HTTP请求功能的插件，支持GET、POST、PUT、DELETE等方法";
    }

    @Override
    public String getAuthor() {
        return "Bolt Team";
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.CUSTOM;
    }

    @Override
    protected void doInitialize() {
        this.defaultTimeout = getProperty("defaultTimeout", 30000);
        this.defaultRetryCount = getProperty("defaultRetryCount", 3);
        logger.info("HttpClientPlugin 初始化完成，默认超时: {}ms", defaultTimeout);
    }

    @Override
    protected void doDestroy() {
        logger.info("HttpClientPlugin 已销毁");
    }

    @Override
    protected void registerActions() {
        registerAction("get", this::doGet);
        registerAction("post", this::doPost);
        registerAction("put", this::doPut);
        registerAction("delete", this::doDelete);
        registerAction("request", this::doRequest);
    }

    /**
     * GET 请求
     */
    private PluginResult doGet(Map<String, Object> params, PluginContext context) {
        String url = getRequiredParam(params, "url");
        Map<String, String> headers = getHeaders(params);
        return executeHttpRequest("GET", url, headers, null, params);
    }

    /**
     * POST 请求
     */
    private PluginResult doPost(Map<String, Object> params, PluginContext context) {
        String url = getRequiredParam(params, "url");
        Map<String, String> headers = getHeaders(params);
        String body = (String) params.get("body");
        return executeHttpRequest("POST", url, headers, body, params);
    }

    /**
     * PUT 请求
     */
    private PluginResult doPut(Map<String, Object> params, PluginContext context) {
        String url = getRequiredParam(params, "url");
        Map<String, String> headers = getHeaders(params);
        String body = (String) params.get("body");
        return executeHttpRequest("PUT", url, headers, body, params);
    }

    /**
     * DELETE 请求
     */
    private PluginResult doDelete(Map<String, Object> params, PluginContext context) {
        String url = getRequiredParam(params, "url");
        Map<String, String> headers = getHeaders(params);
        return executeHttpRequest("DELETE", url, headers, null, params);
    }

    /**
     * 通用请求
     */
    private PluginResult doRequest(Map<String, Object> params, PluginContext context) {
        String method = getRequiredParam(params, "method").toUpperCase();
        String url = getRequiredParam(params, "url");
        Map<String, String> headers = getHeaders(params);
        String body = (String) params.get("body");
        return executeHttpRequest(method, url, headers, body, params);
    }

    /**
     * 执行 HTTP 请求
     */
    private PluginResult executeHttpRequest(String method, String url,
            Map<String, String> headers, String body, Map<String, Object> params) {

        int timeout = (Integer) params.getOrDefault("timeout", defaultTimeout);
        int maxRetries = (Integer) params.getOrDefault("retryCount", defaultRetryCount);

        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            attempt++;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setDoInput(true);

                // 设置请求头
                headers.forEach(conn::setRequestProperty);

                // 发送请求体
                if (body != null && !body.isEmpty()) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }

                // 读取响应
                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? conn.getInputStream()
                                : conn.getErrorStream(),
                        StandardCharsets.UTF_8));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Map<String, Object> result = new HashMap<>();
                result.put("statusCode", responseCode);
                result.put("body", response.toString());
                result.put("headers", conn.getHeaderFields());
                result.put("attempt", attempt);

                return PluginResult.success(result);

            } catch (Exception e) {
                lastException = e;
                logger.warn("HTTP 请求失败 (尝试 {}/{}): {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000L * attempt); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return PluginResult.failure("HTTP_REQUEST_FAILED",
                "请求失败 (尝试 " + attempt + " 次): " + lastException.getMessage());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getHeaders(Map<String, Object> params) {
        Object headersObj = params.get("headers");
        if (headersObj instanceof Map) {
            Map<String, String> headers = new HashMap<>();
            ((Map<String, Object>) headersObj).forEach((k, v) -> headers.put(k, String.valueOf(v)));
            return headers;
        }
        return new HashMap<>();
    }

    private String getRequiredParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null || value.toString().isEmpty()) {
            throw new PluginException("MISSING_PARAM", "缺少必需参数: " + key);
        }
        return value.toString();
    }
}
