package eu.maveniverse.maven.executor.providers.testcontainers;

import static java.util.Objects.requireNonNull;

import com.github.dockerjava.api.DockerClient;
import eu.maveniverse.maven.executor.core.Executor;
import eu.maveniverse.maven.executor.core.ExecutorRequest;
import eu.maveniverse.maven.executor.core.ExecutorResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
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

    /**
     * Note: Testcontainers uses Lombok {@code @Sneakythrows}.
     */
    @Override
    public ExecutorResult execute(ExecutorRequest request) throws ExecutionException, InterruptedException {
        requireNonNull(request);

        HashMap<String, String> env = new HashMap<>();
        request.environment().environmentVariables().ifPresent(env::putAll);
        request.invocation().environmentVariables().ifPresent(env::putAll);
        env.put("MAVEN_CONFIG", "/var/maven-home/.m2");

        ArrayList<String> command = new ArrayList<>();
        command.add(request.invocation().cmd());
        command.add("-Duser.home=/var/maven-home");
        command.addAll(request.invocation().args());

        MemoizingOneShotStartupCheckStrategy startupCheckStrategy = new MemoizingOneShotStartupCheckStrategy();
        try (GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("maven:" + mavenVersion))) {
            container
                    .withFileSystemBind(request.environment().userHome().toString(), "/var/maven-home/")
                    .withFileSystemBind(request.cwd().toString(), "/var/maven-project")
                    .withWorkingDirectory("/var/maven-project")
                    .withStartupCheckStrategy(startupCheckStrategy)
                    .withCommand(command.toArray(new String[0]))
                    .withCreateContainerCmdModifier(cmd -> cmd.withUser(
                            Integer.toString(detectUid(request.environment().userHome()))))
                    .withEnv(env)
                    .start();

            return new ExecutorResult(
                    request,
                    startupCheckStrategy.lastStatus.get() == StartupCheckStrategy.StartupStatus.SUCCESSFUL ? 0 : 1,
                    container.getLogs(OutputFrame.OutputType.STDOUT),
                    container.getLogs(OutputFrame.OutputType.STDERR));
        }
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
