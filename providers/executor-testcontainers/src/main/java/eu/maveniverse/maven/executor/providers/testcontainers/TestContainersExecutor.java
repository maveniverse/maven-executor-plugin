package eu.maveniverse.maven.executor.providers.testcontainers;

import static java.util.Objects.requireNonNull;

import com.github.dockerjava.api.DockerClient;
import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.Executor;
import eu.maveniverse.maven.executor.core.ExecutorResult;
import eu.maveniverse.maven.executor.core.Invocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * Executor that uses {@link org.testcontainers.Testcontainers} to run Maven Docker image.
 */
public class TestContainersExecutor implements Executor {
    private final String mavenVersion;

    public static TestContainersExecutor withMavenVersion(String mavenVersion) {
        return new TestContainersExecutor(mavenVersion);
    }

    private TestContainersExecutor(String mavenVersion) {
        this.mavenVersion = requireNonNull(mavenVersion);
    }

    @Override
    public CompletableFuture<ExecutorResult> execute(Path cwd, Invocation invocation, Environment environment) {
        requireNonNull(cwd);
        requireNonNull(invocation);
        requireNonNull(environment);

        CompletableFuture<ExecutorResult> result = new CompletableFuture<>();
        Path normalizedCwd = cwd.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalizedCwd)) {
            throw new IllegalArgumentException("cwd must be an existing directory");
        }

        HashMap<String, String> env = new HashMap<>();
        environment.environmentVariables().ifPresent(env::putAll);
        invocation.environmentVariables().ifPresent(env::putAll);
        env.put("MAVEN_CONFIG", "/var/maven-home/.m2");

        ArrayList<String> command = new ArrayList<>();
        command.add(invocation.cmd());
        command.add("-Duser.home=/var/maven-home");
        command.addAll(invocation.args());

        MemoizingOneShotStartupCheckStrategy startupCheckStrategy = new MemoizingOneShotStartupCheckStrategy();
        try (GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("maven:" + mavenVersion))
                .withFileSystemBind(environment.userHome().toString(), "/var/maven-home/")
                .withFileSystemBind(normalizedCwd.toString(), "/var/maven-project")
                .withWorkingDirectory("/var/maven-project")
                .withStartupCheckStrategy(startupCheckStrategy)
                .withCommand(command.toArray(new String[0]))
                .withCreateContainerCmdModifier(
                        cmd -> cmd.withUser(Integer.toString(detectUid(environment.userHome()))))
                .withEnv(env)) {
            container.start();

            result.complete(new ExecutorResult(
                    cwd,
                    invocation,
                    environment,
                    startupCheckStrategy.lastStatus.get() == StartupCheckStrategy.StartupStatus.SUCCESSFUL ? 0 : 1,
                    container.getLogs(OutputFrame.OutputType.STDOUT),
                    container.getLogs(OutputFrame.OutputType.STDERR)));
        }
        return result;
    }

    private static int detectUid(Path userHome) {
        try {
            return (Integer) Files.getAttribute(userHome, "unix:uid");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {}

    private static class MemoizingOneShotStartupCheckStrategy extends OneShotStartupCheckStrategy {
        private final AtomicReference<StartupStatus> lastStatus = new AtomicReference<>(null);

        public StartupStatus checkStartupState(DockerClient dockerClient, String containerId) {
            StartupStatus startupStatus = super.checkStartupState(dockerClient, containerId);
            lastStatus.set(startupStatus);
            return startupStatus;
        }
    }
}
