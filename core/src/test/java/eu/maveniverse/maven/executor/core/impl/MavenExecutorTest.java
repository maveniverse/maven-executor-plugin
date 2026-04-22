package eu.maveniverse.maven.executor.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.Invocation;
import java.nio.file.Path;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class MavenExecutorTest {
    protected void doIt(Function<Path, MavenExecutor> me) {
        Path cwd = Path.of("target/test-classes/simple-project");
        Invocation invocation =
                Invocation.ofMvn().withArgs("-V", "clean", "install").build();
        Environment environment = Environment.ofUserHome(cwd).build();
        try (MavenExecutor executor = me.apply(Path.of(System.getProperty("maven.home")))) {
            assertEquals(0, executor.execute(cwd, invocation, environment));
        }
    }

    @Test
    void embedded() {
        doIt(MavenExecutor::embeddedWithMavenInstallation);
    }

    @Test
    void forked() {
        doIt(MavenExecutor::forkedWithMavenInstallation);
    }
}
