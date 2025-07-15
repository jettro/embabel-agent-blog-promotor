# Agent Blog Promotor

A Java 21 Maven project for agent blog promotor functionality.

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
    │   │               └── App.java
    │   └── resources/
    └── test/
        ├── java/
        │   └── dev/
        │       └── jettro/
        │           └── blogpromotor/
        │               └── AppTest.java
        └── resources/
```

## License

This project is licensed under the MIT License.
