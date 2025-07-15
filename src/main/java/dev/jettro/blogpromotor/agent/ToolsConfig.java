package dev.jettro.blogpromotor.agent;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@Configuration
public class ToolsConfig {

    private final List<McpSyncClient> mcpSyncClients;

    @Autowired
    public ToolsConfig(@Lazy List<McpSyncClient> mcpSyncClients) {
        Assert.notNull(mcpSyncClients, "McpSyncClients must not be null");
        this.mcpSyncClients = mcpSyncClients;
    }

    @Bean(name = "mcpTimeToolsGroup")
    public ToolGroup mcpTimeToolsGroup() {
        return new McpToolGroup(
                ToolGroupDescription.Companion.invoke(
                        "A collection of tools to interact with the MCP time service",
                        "mcp-time"
                ),
                "Docker",
                "mcp-time",
                Set.of(ToolGroupPermission.HOST_ACCESS),
                mcpSyncClients,
                callback -> callback.getToolDefinition().name().contains("time")
        );
    }

    @Bean(name = "mcpFirecrawlToolsGroup")
    public ToolGroup mcpFirecrawlToolsGroup() {
        return new McpToolGroup(
                ToolGroupDescription.Companion.invoke(
                        "A collection of tools to interact with the MCP Firecrawl service",
                        "mcp-firecrawl"
                ),
                "Docker",
                "mcp-firecrawl",
                Set.of(ToolGroupPermission.HOST_ACCESS, ToolGroupPermission.INTERNET_ACCESS),
                mcpSyncClients,
                callback -> callback.getToolDefinition().name().contains("firecrawl")
        );
    }
}