package eu.maveniverse.maven.executor.testutils;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestProjects {
    private TestProjects() {}

    public static void createSimpleProject(Path cwd) throws IOException {
        requireNonNull(cwd);

        Files.createDirectories(cwd);
        Path pom = cwd.resolve("pom.xml");
        Path submodulePom = cwd.resolve("submodule").resolve("pom.xml");

        Files.writeString(pom, """
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/maven-v4_0_0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <groupId>org.apache.maven.samples</groupId>
  <artifactId>simple-project</artifactId>
  <version>1.0.0-SNAPSHOT</version>

  <packaging>pom</packaging>

  <modules>
    <module>submodule</module>
  </modules>
</project>
""");

        Files.createDirectories(submodulePom.getParent());
        Files.writeString(submodulePom, """
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/maven-v4_0_0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.apache.maven.samples</groupId>
    <artifactId>simple-project</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>submodule</artifactId>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.2</version>
    </dependency>
  </dependencies>
</project>
    """);
    }
}
