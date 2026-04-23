package eu.maveniverse.maven.executor.core;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * The executor to execute {@link Invocation} in {@link Environment}.
 */
public interface Executor extends Closeable {
    /**
     * Executes {@link Invocation} in this given {@link Environment} and returns {@link ExecutorResult}.
     * <p>
     * Whether implementation blocks or executes async, or pools executions, is left to the implementor.
     *
     * @param cwd The path (must be existing directory) where the working directory for execution is.
     * @param invocation The invocation to execute.
     * @param environment The environment to apply.
     * @return The future of {@link ExecutorResult} of the execution.
     */
    CompletableFuture<ExecutorResult> execute(Path cwd, Invocation invocation, Environment environment);
}
