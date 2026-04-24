package eu.maveniverse.maven.executor.providers.dockerexe;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.executor.core.Executor;
import eu.maveniverse.maven.executor.core.ExecutorRequest;
import eu.maveniverse.maven.executor.core.ExecutorResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    public ExecutorResult execute(ExecutorRequest request) throws ExecutionException, InterruptedException {
        requireNonNull(request);

        Path stdOutPath = null;
        Path stdErrPath = null;
        try {
            HashMap<String, String> env = new HashMap<>();
            request.environment().environmentVariables().ifPresent(env::putAll);
            request.invocation().environmentVariables().ifPresent(env::putAll);
            env.put("MAVEN_CONFIG", "/var/maven-home/.m2");

            ArrayList<String> command = new ArrayList<>();
            command.add("docker");
            command.add("run");
            command.add("--rm");
            command.add("-u");
            command.add(Integer.toString(detectUid(request.environment().userHome())));

            for (Map.Entry<String, String> entry : env.entrySet()) {
                command.add("-e");
                command.add(entry.getKey() + "=" + entry.getValue());
            }

            command.add("-v");
            command.add(request.environment().userHome() + ":/var/maven-home/");
            command.add("-v");
            command.add(request.cwd() + ":/var/maven-project");
            command.add("-w");
            command.add("/var/maven-project");
            command.add("maven:" + mavenVersion);
            command.add(request.invocation().cmd());
            command.add("-Duser.home=/var/maven-home");
            command.addAll(request.invocation().args());

            stdOutPath = Files.createTempFile("docker-executor-stdout-", ".log");
            stdErrPath = Files.createTempFile("docker-executor-stderr-", ".log");
            Process process = new ProcessBuilder()
                    .directory(request.cwd().toFile())
                    .command(command)
                    .redirectOutput(stdOutPath.toFile())
                    .redirectError(stdErrPath.toFile())
                    .start();

            int exitCode = process.waitFor();
            String stdOut = Files.readString(stdOutPath);
            String stdErr = Files.readString(stdErrPath);
            return new ExecutorResult(request, exitCode, stdOut, stdErr);
        } catch (IOException e) {
            throw new ExecutionException(e);
        } finally {
            try {
                if (stdOutPath != null) {
                    Files.deleteIfExists(stdOutPath);
                }
                if (stdErrPath != null) {
                    Files.deleteIfExists(stdErrPath);
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static int detectUid(Path userHome) throws IOException {
        return (Integer) Files.getAttribute(userHome, "unix:uid");
    }

    @Override
    public void close() {}
}
