package eu.maveniverse.maven.executor.core.impl;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.Executor;
import eu.maveniverse.maven.executor.core.Invocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DockerExecutor implements Executor {
    private final String mavenVersion;

    public static DockerExecutor withMavenVersion(String mavenVersion) {
        return new DockerExecutor(mavenVersion);
    }

    private DockerExecutor(String mavenVersion) {
        this.mavenVersion = requireNonNull(mavenVersion);
    }

    @Override
    public int execute(Path cwd, Invocation invocation, Environment environment) {
        requireNonNull(cwd);
        requireNonNull(invocation);
        requireNonNull(environment);

        cwd = cwd.toAbsolutePath().normalize();
        if (!Files.isDirectory(cwd)) {
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
            command.add("my-maven-project");
            command.add("-u");
            command.add(Integer.toString(detectUid(environment.userHome())));

            for (Map.Entry<String, String> entry : env.entrySet()) {
                command.add("-e");
                command.add(entry.getKey() + "=" + entry.getValue());
            }

            command.add("-v");
            command.add(environment.userHome() + ":/var/maven-home/");
            command.add("-v");
            command.add(cwd.toAbsolutePath() + ":/var/maven-project");
            command.add("-w");
            command.add("/var/maven-project");
            command.add("maven:" + mavenVersion);
            command.add(invocation.cmd());
            command.add("-Duser.home=/var/maven-home");
            command.addAll(invocation.args());

            return new ProcessBuilder()
                    .directory(cwd.toFile())
                    .command(command)
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static int detectUid(Path userHome) throws IOException {
        return (Integer) Files.getAttribute(userHome, "unix:uid");
    }

    @Override
    public void close() {}
}
