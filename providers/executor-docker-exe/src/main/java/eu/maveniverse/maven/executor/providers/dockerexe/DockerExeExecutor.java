package eu.maveniverse.maven.executor.providers.dockerexe;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.Executor;
import eu.maveniverse.maven.executor.core.ExecutorResult;
import eu.maveniverse.maven.executor.core.Invocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Executor that spawns a process with {@code docker} CLI to run Maven Docker image.
 */
public class DockerExeExecutor implements Executor {
    private final String mavenVersion;

    public static DockerExeExecutor withMavenVersion(String mavenVersion) {
        return new DockerExeExecutor(mavenVersion);
    }

    private DockerExeExecutor(String mavenVersion) {
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

        try {
            HashMap<String, String> env = new HashMap<>();
            environment.environmentVariables().ifPresent(env::putAll);
            invocation.environmentVariables().ifPresent(env::putAll);
            env.put("MAVEN_CONFIG", "/var/maven-home/.m2");

            ArrayList<String> command = new ArrayList<>();
            command.add("docker");
            command.add("run");
            command.add("--rm");
            command.add("--name");
            command.add("my-maven-project"); // TODO: this should be based on some input
            command.add("-u");
            command.add(Integer.toString(detectUid(environment.userHome())));

            for (Map.Entry<String, String> entry : env.entrySet()) {
                command.add("-e");
                command.add(entry.getKey() + "=" + entry.getValue());
            }

            command.add("-v");
            command.add(environment.userHome() + ":/var/maven-home/");
            command.add("-v");
            command.add(normalizedCwd + ":/var/maven-project");
            command.add("-w");
            command.add("/var/maven-project");
            command.add("maven:" + mavenVersion);
            command.add(invocation.cmd());
            command.add("-Duser.home=/var/maven-home");
            command.addAll(invocation.args());

            Path stdOutPath = Files.createTempFile("docker-executor-stdout-", ".log");
            Path stdErrPath = Files.createTempFile("docker-executor-stderr-", ".log");
            Process process = new ProcessBuilder()
                    .directory(normalizedCwd.toFile())
                    .command(command)
                    .redirectOutput(stdOutPath.toFile())
                    .redirectError(stdErrPath.toFile())
                    .start();

            int exitCode = process.waitFor();
            String stdOut = Files.readString(stdOutPath);
            String stdErr = Files.readString(stdErrPath);
            result.complete(new ExecutorResult(normalizedCwd, invocation, environment, exitCode, stdOut, stdErr));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    private static int detectUid(Path userHome) throws IOException {
        return (Integer) Files.getAttribute(userHome, "unix:uid");
    }

    @Override
    public void close() {}
}
