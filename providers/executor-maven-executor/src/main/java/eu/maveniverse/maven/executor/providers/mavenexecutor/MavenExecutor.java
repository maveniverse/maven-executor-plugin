package eu.maveniverse.maven.executor.providers.mavenexecutor;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.executor.core.Executor;
import eu.maveniverse.maven.executor.core.ExecutorResult;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.apache.maven.api.cli.ExecutorException;
import org.apache.maven.api.cli.ExecutorRequest;
import org.apache.maven.cling.executor.embedded.EmbeddedMavenExecutor;
import org.apache.maven.cling.executor.forked.ForkedMavenExecutor;

/**
 * Executor that uses {@code maven-executor}, and can run Maven in "embedded" or "forked" mode. Latter spawns
 * a forked OS process.
 */
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
    public ExecutorResult execute(eu.maveniverse.maven.executor.core.ExecutorRequest request)
            throws ExecutionException {
        requireNonNull(request);
        try {
            HashMap<String, String> env = new HashMap<>();
            request.environment().environmentVariables().ifPresent(env::putAll);
            request.invocation().environmentVariables().ifPresent(env::putAll);

            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
            int exitCode = executor.execute(ExecutorRequest.mavenBuilder(installationDirectory)
                    .cwd(request.cwd())
                    .command(request.invocation().cmd())
                    .arguments(request.invocation().args())
                    .userHomeDirectory(request.environment().userHome())
                    .environmentVariables(env)
                    .skipMavenRc(true)
                    .stdOut(stdOut)
                    .stdErr(stdErr)
                    .build());
            return new ExecutorResult(request, exitCode, stdOut.toString(), stdErr.toString());
        } catch (ExecutorException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public void close() {
        executor.close();
    }
}
