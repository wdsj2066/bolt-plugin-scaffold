package io.bolt.plugin.examples;

import io.bolt.plugin.AbstractPlugin;
import io.bolt.plugin.api.PluginContext;
import io.bolt.plugin.api.PluginException;
import io.bolt.plugin.api.PluginResult;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库插件示例
 * 演示如何创建访问数据库的插件
 * 注意：生产环境请使用连接池
 */
public class DatabasePlugin extends AbstractPlugin {

    private static final String PLUGIN_ID = "database-plugin";
    private static final String VERSION = "1.0.0";

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
        return "数据库插件";
    }

    @Override
    public String getDescription() {
        return "提供数据库查询和操作功能的插件";
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

        // 测试连接
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            if (!conn.isValid(5)) {
                throw new PluginException("CONNECTION_FAILED", "数据库连接测试失败");
            }
        } catch (SQLException e) {
            throw new PluginException("CONNECTION_FAILED", "数据库连接失败: " + e.getMessage(), e);
        }

        logger.info("DatabasePlugin 初始化成功，连接: {}", jdbcUrl);
    }

    @Override
    public String getDefaultConfigJson() {
        return "{\n" +
                "    \"jdbcUrl\": \"jdbc:mysql://localhost:3306/mydb\",\n" +
                "    \"username\": \"root\",\n" +
                "    \"password\": \"your_password\"\n" +
                "}";
    }

    @Override
    protected void doDestroy() {
        // 清理资源（如果有连接池的话）
        logger.info("DatabasePlugin 已销毁");
    }

    @Override
    protected void registerActions() {
        registerAction("query", this::doQuery);
        registerAction("execute", this::doExecute);
        registerAction("update", this::doUpdate);
        registerAction("batch", this::doBatch);
        registerAction("test", this::doTest);
    }

    /**
     * 查询操作
     */
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
            result.put("columns", getColumnNames(metaData, columnCount));

            return PluginResult.success(result);

        } catch (SQLException e) {
            logger.error("查询失败: {}", e.getMessage());
            return PluginResult.failure("QUERY_FAILED", e.getMessage());
        }
    }

    /**
     * 执行操作（DDL等）
     */
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
            return PluginResult.failure("EXECUTE_FAILED", e.getMessage());
        }
    }

    /**
     * 更新操作（INSERT/UPDATE/DELETE）
     */
    private PluginResult doUpdate(Map<String, Object> params, PluginContext context) {
        String sql = getRequiredParam(params, "sql");
        @SuppressWarnings("unchecked")
        List<Object> sqlParams = (List<Object>) params.get("params");

        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, sqlParams)) {

            int affectedRows = stmt.executeUpdate();

            Map<String, Object> result = new HashMap<>();
            result.put("affectedRows", affectedRows);

            return PluginResult.success(result);

        } catch (SQLException e) {
            logger.error("更新失败: {}", e.getMessage());
            return PluginResult.failure("UPDATE_FAILED", e.getMessage());
        }
    }

    /**
     * 批量操作
     */
    @SuppressWarnings("unchecked")
    private PluginResult doBatch(Map<String, Object> params, PluginContext context) {
        String sql = getRequiredParam(params, "sql");
        List<List<Object>> batchParams = (List<List<Object>>) params.get("batchParams");

        if (batchParams == null || batchParams.isEmpty()) {
            return PluginResult.failure("INVALID_PARAM", "批量参数不能为空");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (List<Object> sqlParams : batchParams) {
                setParameters(stmt, sqlParams);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            int totalAffected = 0;
            for (int r : results) {
                if (r > 0) totalAffected += r;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("batchSize", batchParams.size());
            result.put("results", results);
            result.put("totalAffected", totalAffected);

            return PluginResult.success(result);

        } catch (SQLException e) {
            logger.error("批量操作失败: {}", e.getMessage());
            return PluginResult.failure("BATCH_FAILED", e.getMessage());
        }
    }

    /**
     * 连接测试
     */
    private PluginResult doTest(Map<String, Object> params, PluginContext context) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            Map<String, Object> result = new HashMap<>();
            result.put("connected", true);
            result.put("databaseProductName", metaData.getDatabaseProductName());
            result.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            result.put("driverName", metaData.getDriverName());
            result.put("driverVersion", metaData.getDriverVersion());

            return PluginResult.success(result);

        } catch (SQLException e) {
            return PluginResult.failure("TEST_FAILED", e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, List<Object> params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt;
    }

    private void setParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
        }
    }

    private List<String> getColumnNames(ResultSetMetaData metaData, int columnCount) throws SQLException {
        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }
        return columns;
    }

    private String getRequiredParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null || value.toString().isEmpty()) {
            throw new PluginException("MISSING_PARAM", "缺少必需参数: " + key);
        }
        return value.toString();
    }
}
