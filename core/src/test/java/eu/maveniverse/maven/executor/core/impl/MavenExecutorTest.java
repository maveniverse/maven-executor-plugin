package eu.maveniverse.maven.executor.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.Invocation;
import java.nio.file.Path;
import org.apache.maven.api.cli.Executor;
import org.apache.maven.cling.executor.embedded.EmbeddedMavenExecutor;
import org.apache.maven.cling.executor.forked.ForkedMavenExecutor;
import org.junit.jupiter.api.Test;

public class MavenExecutorTest {
    protected void doIt(Executor me) {
        Path cwd = Path.of("target/test-classes/simple-project");
        Invocation invocation =
                Invocation.ofMvn().withArgs("-V", "clean", "install").build();
        Environment environment = Environment.ofUserHome(cwd).build();
        try (MavenExecutor executor = new MavenExecutor(me, Path.of(System.getProperty("maven.home")))) {
            assertEquals(0, executor.execute(cwd, invocation, environment));
        }
    }

    @Test
    void embedded() {
        doIt(new EmbeddedMavenExecutor(true, false));
    }

    @Test
    void forked() {
        doIt(new ForkedMavenExecutor(false));
    }
}
