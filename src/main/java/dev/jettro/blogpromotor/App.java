package dev.jettro.blogpromotor;

import com.embabel.agent.config.annotation.EnableAgents;
import com.embabel.agent.config.annotation.LoggingThemes;
import com.embabel.agent.config.annotation.McpServers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Agent Blog Promoter
 */
@SpringBootApplication
@EnableAgents(loggingTheme = LoggingThemes.STAR_WARS, mcpServers = McpServers.DOCKER)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
