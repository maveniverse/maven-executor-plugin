package eu.maveniverse.maven.executor.core.impl;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.executor.core.Environment;
import eu.maveniverse.maven.executor.core.Executor;
import eu.maveniverse.maven.executor.core.Invocation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.apache.maven.api.cli.ExecutorRequest;
import org.apache.maven.cling.executor.embedded.EmbeddedMavenExecutor;
import org.apache.maven.cling.executor.forked.ForkedMavenExecutor;

public class MavenExecutor implements Executor {
    private final org.apache.maven.api.cli.Executor executor;
    private final Path installationDirectory;

    public static MavenExecutor forkedWithMavenInstallation(Path installationDirectory) {
        return new MavenExecutor(new ForkedMavenExecutor(false), installationDirectory);
    }

    public static MavenExecutor embeddedWithMavenInstallation(Path installationDirectory) {
        return new MavenExecutor(new EmbeddedMavenExecutor(true, false), installationDirectory);
    }

    private MavenExecutor(org.apache.maven.api.cli.Executor executor, Path installationDirectory) {
        this.executor = requireNonNull(executor);
        this.installationDirectory = requireNonNull(installationDirectory);
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

        HashMap<String, String> env = new HashMap<>();
        environment.environmentVariables().ifPresent(env::putAll);
        invocation.environmentVariables().ifPresent(env::putAll);

        return executor.execute(ExecutorRequest.mavenBuilder(installationDirectory)
                .cwd(cwd)
                .command(invocation.cmd())
                .arguments(invocation.args())
                .userHomeDirectory(environment.userHome())
                .environmentVariables(env)
                .stdOut(System.out)
                .stdErr(System.err)
                .build());
    }

    @Override
    public void close() {
        executor.close();
    }
}
