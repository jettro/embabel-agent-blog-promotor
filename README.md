# Agent Blog Promotor

Agent Blog Promotor is a Java 21 Maven project that automates the process of promoting blog posts on social media platforms. It leverages agent-based personas to extract blog content, craft engaging social posts, select the best images, and review posts for maximum engagement.

## Features

- **Automated Blog Extraction:** Fetches blog content and images from a given URL.
- **Social Post Generation:** Crafts concise, engaging social media posts tailored for platforms like LinkedIn.
- **Image Selection:** Uses AI to select the most relevant image from the blog post.
- **Content Review:** Reviews and refines posts for engagement and professionalism.
- **Extensible Personas:** Modular agent personas for extraction, writing, and reviewing.
- **PII Obfuscation:** Protects PII from being exposed in social media posts.

## Technologies Used

- Java 21
- Maven
- Spring (for configuration and profiles)
- Custom agent and persona framework (Embabel)
- JUnit 5 for testing
- Microsoft Presidio for Obfuscation in the PII Guardrail

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Docker for running Presidio (Optional)

## Building the Project

```bash
mvn clean compile
```

## Running Tests

```bash
mvn test
```

## Running the Application

```bash
mvn exec:java -Dexec.mainClass="dev.jettro.blogpromotor.App"
```

## Enabling Optional Features

You can enable additional features such as Guardrails and Observability using Spring profiles.

### Guardrails (PII Protection)

The `guardrails` profile enables PII (Personally Identifiable Information) protection using Microsoft Presidio. When enabled, the agent will analyze user input for sensitive data and mask it before sending it to the LLM.

To enable Guardrails:

1.  **Start Microsoft Presidio Analyzer:**
    ```bash
    docker pull mcr.microsoft.com/presidio-analyzer
    docker run -d -p 5002:3000 mcr.microsoft.com/presidio-analyzer:latest
    ```
2.  **Activate the profile:**
    Add `guardrails` to `spring.profiles.active` in `src/main/resources/application.yml` or run with:
    ```bash
    mvn exec:java -Dexec.mainClass="dev.jettro.blogpromotor.App" -Dspring.profiles.active=guardrails
    ```

Find more information about the PII Guardrail [here](https://microsoft.github.io/presidio/getting_started/getting_started_text/).

### Observability (Tracing)

The `tracing` profile enables observability via OpenTelemetry and Langfuse. This allows you to track agent execution and LLM calls.

To enable Observability:

1.  **Configure Langfuse credentials:**
    Set the following environment variables:
    - `LANGFUSE_BASE_URL`
    - `LANGFUSE_PUBLIC_KEY`
    - `LANGFUSE_SECRET_KEY`
2.  **Activate the profile:**
    Add `tracing` to `spring.profiles.active` in `src/main/resources/application.yml` or run with:
    ```bash
    mvn exec:java -Dexec.mainClass="dev.jettro.blogpromotor.App" -Dspring.profiles.active=tracing
    ```

You can enable both features simultaneously by activating both profiles: `-Dspring.profiles.active=guardrails,tracing`.

## Agent Workflow

1. **Fetch Blog Post:** Extracts main content and images from a blog URL.
2. **Craft Social Post:** Generates a social media post based on the blog content.
3. **Select Best Image:** Chooses the most relevant image for the post.
4. **Review Post:** Reviews the post for engagement and professionalism.
5. **Construct Social Media Post:** Combines reviewed content and image for publishing.

## Example Usage

You can trigger the workflow by providing a blog URL as user input. The agent will process the blog and output a ready-to-publish social media post.

## Project Structure

```
agent-blog-promotor/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/
    │   │   └── dev/
    │   │       └── jettro/
    │   │           └── blogpromotor/
    │   │               ├── App.java
    │   │               └── agent/
    │   │                   ├── BlogPromoterAgent.java
    │   │                   ├── ToolsConfig.java
    │   └── resources/
    └── test/
        └── java/
            └── dev/
                └── jettro/
                    └── blogpromotor/
                        └── agent/
                            └── BlogPromoterAgentTest.java
```

## Contributing

Contributions are welcome! Please open issues or submit pull requests for improvements or bug fixes.

## License

This project is licensed under the MIT License.


# Migration notes

## Migrate to 0.3.5
- The sample includes a profile to enable observability through OpenTelemetry and Langfuse.

## Migrate from 0.2.0 to 0.3.1
- Persona instances are now created using the **Persona.create(..)** method. This replaces the old way via **new Persona(..)**.
- Package name for utility classes to make testing easier changed from com.embabel.agent.**testing**.unit to com.embabel.agent.**test**.unit

## Migrate from 0.1.4 to 0.2.0
The most important change is that artifacts are now available in maven central. No need for other repositories.

## Migrate from 0.1.1 to 0.1.4

### Dependency Changes
There are changes in the maven dependencies. Embabel now has more starters to include only what is needed. One improvement is related to the inclusion of Ollama dependencies. Another is in projects where the shell is not needed, you can just omit the dependency. One example to do this is when exposing the agent as an MCP server.

You can remove the @EnableAgentShell annotation from your main class. If you want to keep the shell, you can just add the new dependency.

```xml
<dependency>
    <groupId>com.embabel.agent</groupId>
    <artifactId>embabel-agent-starter-shell</artifactId>
    <version>${embabel-agent.version}</version>
</dependency>
```

### Persona Class Changes
The persona class was changed. The create method is removed, you can now use the public constructor.

Before:

```java
Persona.create(...)
```

After:

```java
new Persona(...)
```

### Unit test changes
In unit testing, you want to check the content of the prompt. The getPrompt method was removed. You can now get the prompt from the tool directly.

Before:

```java
persona.getPrompt();
```
After:

```java
promptRunner.getLlmInvocations().getFirst().getMessages().getFirst().getContent();
```
