package io.bolt.plugin.examples;

import io.bolt.plugin.AbstractPlugin;
import io.bolt.plugin.api.PluginContext;
import io.bolt.plugin.api.PluginException;
import io.bolt.plugin.api.PluginResult;
import io.bolt.plugin.api.node.*;

import java.sql.*;
import java.util.*;

public class ModernDatabasePlugin extends AbstractPlugin {

    private static final String PLUGIN_ID = "modern-database-plugin";
    private static final String VERSION = "2.0.0";

    private String jdbcUrl;
    private String username;
    private String password;

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
        return "现代数据库插件";
    }

    @Override
    public String getDescription() {
        return "使用新架构的数据库插件，提供节点定义和执行器";
    }

    @Override
    public String getAuthor() {
        return "Bolt Team";
    }

    @Override
    public String getPluginType() {
        return "DATASOURCE";
    }

    @Override
    protected void doInitialize() {
        this.jdbcUrl = getRequiredProperty("jdbcUrl");
        this.username = getRequiredProperty("username");
        this.password = getRequiredProperty("password");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            if (!conn.isValid(5)) {
                throw new PluginException("CONNECTION_FAILED", "数据库连接测试失败");
            }
        } catch (SQLException e) {
            throw new PluginException("CONNECTION_FAILED", "数据库连接失败: " + e.getMessage(), e);
        }

        logger.info("ModernDatabasePlugin 初始化成功，连接: {}", jdbcUrl);
    }

    @Override
    protected void registerActions() {
        registerAction("query", this::doQuery);
        registerAction("execute", this::doExecute);
    }

    @Override
    public NodeProvider getNodeProvider() {
        return new ModernDatabaseNodeProvider();
    }

    private PluginResult doQuery(Map<String, Object> params, PluginContext context) {
        String sql = getRequiredParam(params, "sql");
        @SuppressWarnings("unchecked")
        List<Object> sqlParams = (List<Object>) params.get("params");

        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, sqlParams);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rows", rows);
            result.put("count", rows.size());

            return PluginResult.success(result);

        } catch (SQLException e) {
            logger.error("查询失败: {}", e.getMessage());
            return PluginResult.failure("QUERY_FAILED", e.getMessage());
        }
    }

    private PluginResult doExecute(Map<String, Object> params, PluginContext context) {
        String sql = getRequiredParam(params, "sql");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            boolean hasResultSet = stmt.execute(sql);

            Map<String, Object> result = new HashMap<>();
            result.put("hasResultSet", hasResultSet);

            if (!hasResultSet) {
                result.put("updateCount", stmt.getUpdateCount());
            }

            return PluginResult.success(result);

        } catch (SQLException e) {
            logger.error("执行失败: {}", e.getMessage());
            return PluginResult.failure("EXECUTION_FAILED", e.getMessage());
        }
    }

    @Override
    protected void doDestroy() throws Exception {
        logger.info("ModernDatabasePlugin 销毁");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, List<Object> params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
        }
        return stmt;
    }

    private String getRequiredParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null || value.toString().isEmpty()) {
            throw new PluginException("MISSING_PARAM", "缺少必需参数: " + key);
        }
        return value.toString();
    }

    private class ModernDatabaseNodeProvider implements NodeProvider {

        @Override
        public List<NodeDefinition> getNodeDefinitions() {
            return List.of(
                    buildQueryNodeDefinition(),
                    buildExecuteNodeDefinition()
            );
        }

        @Override
        public NodeExecutor getNodeExecutor(String nodeId) {
            String queryNodeId = PLUGIN_ID + ".query";
            String executeNodeId = PLUGIN_ID + ".execute";
            if (queryNodeId.equals(nodeId)) {
                return new QueryNodeExecutor();
            } else if (executeNodeId.equals(nodeId)) {
                return new ExecuteNodeExecutor();
            }
            return null;
        }

        @Override
        public String getProviderId() {
            return PLUGIN_ID;
        }

        @Override
        public String getProviderVersion() {
            return VERSION;
        }

        private NodeDefinition buildQueryNodeDefinition() {
            Map<String, Object> inputSchema = new HashMap<>();
            inputSchema.put("type", "object");
            inputSchema.put("properties", Map.of(
                    "sql", Map.of(
                            "type", "string",
                            "description", "要执行的SELECT查询语句",
                            "example", "SELECT * FROM users WHERE id = ?"
                    ),
                    "params", Map.of(
                            "type", "array",
                            "description", "SQL语句中的参数值列表"
                    )
            ));
            inputSchema.put("required", List.of("sql"));

            return NodeDefinition.builder()
                    .nodeId(PLUGIN_ID + ".query")
                    .displayName("查询数据")
                    .description("执行SQL查询语句，返回查询结果")
                    .category("database")
                    .version(VERSION)
                    .providerId(PLUGIN_ID)
                    .inputSchema(inputSchema)
                    .capabilities(Map.of(
                            "supportsAsync", true,
                            "supportsCancel", false,
                            "defaultTimeoutMs", 30000
                    ))
                    .enabled(true)
                    .build();
        }

        private NodeDefinition buildExecuteNodeDefinition() {
            Map<String, Object> inputSchema = new HashMap<>();
            inputSchema.put("type", "object");
            inputSchema.put("properties", Map.of(
                    "sql", Map.of(
                            "type", "string",
                            "description", "要执行的SQL语句（DDL、DML等）"
                    )
            ));
            inputSchema.put("required", List.of("sql"));

            return NodeDefinition.builder()
                    .nodeId(PLUGIN_ID + ".execute")
                    .displayName("执行SQL")
                    .description("执行DDL或其他SQL语句")
                    .category("database")
                    .version(VERSION)
                    .providerId(PLUGIN_ID)
                    .inputSchema(inputSchema)
                    .capabilities(Map.of(
                            "supportsAsync", false,
                            "supportsCancel", false,
                            "defaultTimeoutMs", 60000
                    ))
                    .enabled(true)
                    .build();
        }

        private class QueryNodeExecutor implements NodeExecutor {

            @Override
            public String getNodeId() {
                return PLUGIN_ID + ".query";
            }

            @Override
            public NodeResult doExecute(NodeContext context, Map<String, Object> input) throws Exception {
                String sql = (String) input.get("sql");
                @SuppressWarnings("unchecked")
                List<Object> params = (List<Object>) input.get("params");

                try (Connection conn = getConnection();
                     PreparedStatement stmt = prepareStatement(conn, sql, params);
                     ResultSet rs = stmt.executeQuery()) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    List<Map<String, Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnLabel(i), rs.getObject(i));
                        }
                        rows.add(row);
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("rows", rows);
                    result.put("count", rows.size());

                    return NodeResult.success(result);
                }
            }

            @Override
            public boolean supportsAsync() {
                return true;
            }

            @Override
            public long getDefaultTimeoutMs() {
                return 30000;
            }
        }

        private class ExecuteNodeExecutor implements NodeExecutor {

            @Override
            public String getNodeId() {
                return PLUGIN_ID + ".execute";
            }

            @Override
            public NodeResult doExecute(NodeContext context, Map<String, Object> input) throws Exception {
                String sql = (String) input.get("sql");

                try (Connection conn = getConnection();
                     Statement stmt = conn.createStatement()) {

                    boolean hasResultSet = stmt.execute(sql);

                    Map<String, Object> result = new HashMap<>();
                    result.put("hasResultSet", hasResultSet);

                    if (!hasResultSet) {
                        result.put("updateCount", stmt.getUpdateCount());
                    }

                    return NodeResult.success(result);
                }
            }

            @Override
            public long getDefaultTimeoutMs() {
                return 60000;
            }
        }
    }
}
