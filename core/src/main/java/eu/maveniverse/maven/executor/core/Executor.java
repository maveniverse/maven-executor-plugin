package eu.maveniverse.maven.executor.core;

import java.io.Closeable;
import java.nio.file.Path;

/**
 * The executor to execute {@link Invocation} in {@link Environment}.
 */
public interface Executor extends Closeable {
    /**
     * Executes {@link Invocation} in this given {@link Environment}.
     *
     * @param cwd The path (must be existing directory) where the working directory for execution is.
     * @param invocation The invocation to execute.
     * @param environment The environment to apply.
     * @return The exit code of the execution.
     */
    int execute(Path cwd, Invocation invocation, Environment environment);
}
