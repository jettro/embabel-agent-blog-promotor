# Agent Blog Promotor

Agent Blog Promotor is a Java 21 Maven project that automates the process of promoting blog posts on social media platforms. It leverages agent-based personas to extract blog content, craft engaging social posts, select the best images, and review posts for maximum engagement.

## Features

- **Automated Blog Extraction:** Fetches blog content and images from a given URL.
- **Social Post Generation:** Crafts concise, engaging social media posts tailored for platforms like LinkedIn.
- **Image Selection:** Uses AI to select the most relevant image from the blog post.
- **Content Review:** Reviews and refines posts for engagement and professionalism.
- **Extensible Personas:** Modular agent personas for extraction, writing, and reviewing.

## Technologies Used

- Java 21
- Maven
- Spring (for configuration and profiles)
- Custom agent and persona framework (Embabel)
- JUnit 5 for testing

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

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
