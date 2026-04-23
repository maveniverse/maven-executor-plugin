package eu.maveniverse.maven.executor.providers.mavenexecutor;

import static eu.maveniverse.maven.executor.core.ExecutorResult.stripAnsi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.ExecutorResult;
import eu.maveniverse.maven.executor.core.Invocation;
import eu.maveniverse.maven.executor.testutils.TestProjects;
import java.nio.file.Path;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class MavenExecutorTest {
    protected void doIt(Function<Path, MavenExecutor> me) throws Exception {
        Path cwd = Path.of("target/test-classes/simple-project");
        TestProjects.createSimpleProject(cwd);
        Invocation invocation =
                Invocation.ofMvn().withArgs("-V", "clean", "install").build();
        Environment environment = Environment.ofUserHome(cwd).build();
        try (MavenExecutor executor = me.apply(Path.of(System.getProperty("maven.home")))) {
            ExecutorResult result =
                    executor.execute(cwd, invocation, environment).get();
            assertEquals(0, result.exitCode());
            assertTrue(stripAnsi(result.getStdOut()).contains("[INFO] BUILD SUCCESS"));
        }
    }

    @Test
    void embedded() throws Exception {
        doIt(MavenExecutor::embeddedWithMavenInstallation);
    }

    @Test
    void forked() throws Exception {
        doIt(MavenExecutor::forkedWithMavenInstallation);
    }
}
