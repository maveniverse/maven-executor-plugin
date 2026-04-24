package eu.maveniverse.maven.executor.core;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

/**
 * Executor results.
 */
public class ExecutorResult {
    private final ExecutorRequest request;
    private final int exitCode;
    private final String stdOut;
    private final String stdErr;

    public ExecutorResult(ExecutorRequest request, int exitCode, String stdOut, String stdErr) {
        this.request = requireNonNull(request);
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public Path cwd() {
        return request.cwd();
    }

    public Invocation invocation() {
        return request.invocation();
    }

    public Environment environment() {
        return request.environment();
    }

    public ExecutorRequest request() {
        return request;
    }

    public int exitCode() {
        return exitCode;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    /**
     * Helper method to strip ANSI color codes from standard output or error strings. It is up to user to configure
     * executed tool to behave as user wants, and hence, user should take care also what output is to be expected.
     */
    public static String stripAnsi(String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
