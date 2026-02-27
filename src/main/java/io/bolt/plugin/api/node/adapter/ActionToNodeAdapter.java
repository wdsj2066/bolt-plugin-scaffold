package io.bolt.plugin.api.node.adapter;

import io.bolt.plugin.AbstractPlugin;
import io.bolt.plugin.api.PluginContext;
import io.bolt.plugin.api.PluginResult;
import io.bolt.plugin.api.node.*;

import java.util.*;

public class ActionToNodeAdapter implements NodeProvider {

    private final AbstractPlugin plugin;
    private final List<NodeDefinition> nodeDefinitions;

    public ActionToNodeAdapter(AbstractPlugin plugin) {
        this.plugin = plugin;
        this.nodeDefinitions = new ArrayList<>();
    }

    public void addActionNode(String actionName, String displayName, String description, 
                            Map<String, Object> inputSchema) {
        String nodeId = plugin.getPluginId() + "." + actionName;
        
        NodeDefinition definition = NodeDefinition.builder()
                .nodeId(nodeId)
                .displayName(displayName)
                .description(description)
                .category(plugin.getPluginType().toLowerCase())
                .version(plugin.getVersion())
                .providerId(plugin.getPluginId())
                .inputSchema(inputSchema)
                .enabled(true)
                .build();
        
        nodeDefinitions.add(definition);
    }

    @Override
    public List<NodeDefinition> getNodeDefinitions() {
        return new ArrayList<>(nodeDefinitions);
    }

    @Override
    public NodeExecutor getNodeExecutor(String nodeId) {
        if (nodeId == null || !nodeId.startsWith(plugin.getPluginId() + ".")) {
            return null;
        }

        String actionName = nodeId.substring(plugin.getPluginId().length() + 1);
        
        return new NodeExecutor() {
            @Override
            public String getNodeId() {
                return nodeId;
            }

            @Override
            public NodeResult doExecute(NodeContext context, Map<String, Object> input) throws Exception {
                PluginContext pluginContext = PluginContext.forWorkflowNode(context.getInstanceId(), context.getNodeId());
                
                PluginResult pluginResult = plugin.execute(actionName, input, pluginContext);
                
                if (pluginResult.isSuccess()) {
                    return NodeResult.success(pluginResult.getData());
                } else {
                    return NodeResult.failure(
                            pluginResult.getErrorCode(),
                            pluginResult.getError()
                    );
                }
            }

            @Override
            public long getDefaultTimeoutMs() {
                return 30000;
            }
        };
    }

    @Override
    public String getProviderId() {
        return plugin.getPluginId();
    }

    @Override
    public String getProviderVersion() {
        return plugin.getVersion();
    }
}
