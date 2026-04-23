package eu.maveniverse.maven.executor.providers.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.ExecutorResult;
import eu.maveniverse.maven.executor.core.Invocation;
import eu.maveniverse.maven.executor.testutils.TestProjects;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class TestContainersExecutorTest {
    @Test
    void smoke() throws Exception {
        Path cwd = Path.of("target/test-classes/simple-project");
        TestProjects.createSimpleProject(cwd);
        Invocation invocation =
                Invocation.ofMvn().withArgs("-V", "clean", "install").build();
        Environment environment = Environment.ofUserHome(cwd).build();
        try (TestContainersExecutor executor = TestContainersExecutor.withMavenVersion("3.9.15")) {
            ExecutorResult result =
                    executor.execute(cwd, invocation, environment).get();
            assertEquals(0, result.exitCode());
            assertTrue(result.getStdOut().contains("[INFO] BUILD SUCCESS"));
        }
    }
}
