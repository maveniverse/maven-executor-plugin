package eu.maveniverse.maven.executor.core;

import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The executor request.
 */
public interface ExecutorRequest {
    /**
     * The current working directory where the execution should happen.
     */
    Path cwd();

    /**
     * The invocation, that should happen during execution.
     */
    Invocation invocation();

    /**
     * The environment, in which execution should happen.
     */
    Environment environment();

    /**
     * Returns the builder with given current working directory, that must be existing directory.
     */
    static Builder ofCwd(Path cwd) {
        return new Builder(cwd);
    }

    class Builder {
        private final Path cwd;
        private Invocation invocation;
        private Environment environment;

        private Builder(Path cwd) {
            Path normalizedCwd = cwd.toAbsolutePath().normalize();
            if (!Files.isDirectory(normalizedCwd)) {
                throw new IllegalArgumentException("cwd must be an existing directory");
            }
            this.cwd = requireNonNull(normalizedCwd);
        }

        public ExecutorRequest build() {
            if (invocation == null) {
                throw new IllegalStateException("invocation must be set");
            }
            if (environment == null) {
                throw new IllegalStateException("environment must be set");
            }
            return new Impl(cwd, invocation, environment);
        }

        public Builder withInvocation(Invocation invocation) {
            this.invocation = requireNonNull(invocation);
            return this;
        }

        public Builder withEnvironment(Environment environment) {
            this.environment = requireNonNull(environment);
            return this;
        }

        private static class Impl implements ExecutorRequest {
            private final Path cwd;
            private final Invocation invocation;
            private final Environment environment;

            private Impl(Path cwd, Invocation invocation, Environment environment) {
                this.cwd = cwd;
                this.invocation = invocation;
                this.environment = environment;
            }

            @Override
            public Path cwd() {
                return cwd;
            }

            @Override
            public Invocation invocation() {
                return invocation;
            }

            @Override
            public Environment environment() {
                return environment;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Impl impl)) {
                    return false;
                }
                return Objects.equals(cwd, impl.cwd)
                        && Objects.equals(invocation, impl.invocation)
                        && Objects.equals(environment, impl.environment);
            }

            @Override
            public int hashCode() {
                return Objects.hash(cwd, invocation, environment);
            }
        }
    }
}
