package eu.maveniverse.maven.executor.core;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * The executor to execute {@link Invocation} in {@link Environment}.
 */
public interface Executor extends Closeable {

    /**
     * Record carrying result.
     *
     * @param exitCode The execution exit code, should be 0 in case all went ok. Still, the fact "is it really okay?"
     *                 check is left to caller (ie some condition fulfillment). This may be not the exit code of the
     *                 command (ie docker) and only denotes that invocation did not fail (for any reason).
     * @param cwd The cwd path execution was invoked with.
     * @param invocation The invocation this result comes from.
     * @param environment The environment this result comes from.
     */
    record Result(int exitCode, Path cwd, Invocation invocation, Environment environment) {
        public Result(int exitCode, Path cwd, Invocation invocation, Environment environment) {
            this.exitCode = exitCode;
            this.cwd = requireNonNull(cwd);
            this.invocation = requireNonNull(invocation);
            this.environment = requireNonNull(environment);
        }
    }

    /**
     * Executes {@link Invocation} in this given {@link Environment} and returns {@link Result}.
     * <p>
     * Whether implementation blocks or executes async, or even pools executions, is left to the implementor.
     *
     * @param cwd The path (must be existing directory) where the working directory for execution is.
     * @param invocation The invocation to execute.
     * @param environment The environment to apply.
     * @return The future of {@link Result} of the execution.
     */
    CompletableFuture<Result> execute(Path cwd, Invocation invocation, Environment environment);
}
