package eu.maveniverse.maven.executor.core;

import java.io.Closeable;
import java.util.concurrent.ExecutionException;

/**
 * The executor to execute {@link Invocation} in {@link Environment}.
 */
public interface Executor extends Closeable {
    /**
     * Executes {@link Invocation} in this given {@link Environment} and returns {@link ExecutorResult}.
     * <p>
     * Whether implementation blocks or executes async is left to the implementor.
     *
     * @param request The {@link ExecutorRequest}, may not be {@code null}.
     * @throws ExecutionException In case of failure during execution.
     * @throws InterruptedException In case execution got interrupted.
     * @return The future of {@link ExecutorResult} of the execution.
     */
    ExecutorResult execute(ExecutorRequest request) throws ExecutionException, InterruptedException;
}
