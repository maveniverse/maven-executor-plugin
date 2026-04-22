package eu.maveniverse.maven.executor.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.Invocation;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class DockerExecutorTest {
    @Test
    void smoke() {
        Path cwd = Path.of("target/test-classes/simple-project");
        Invocation invocation =
                Invocation.ofMvn().withArgs("-V", "clean", "install").build();
        Environment environment = Environment.ofUserHome(cwd).build();
        try (DockerExecutor executor = DockerExecutor.withMavenVersion("3.9.15")) {
            assertEquals(0, executor.execute(cwd, invocation, environment));
        }
    }
}
