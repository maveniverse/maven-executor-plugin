package eu.maveniverse.maven.executor.core;

import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The environment to execute {@link Invocation}s.
 */
public interface Environment {
    /**
     * The user home directory, never {@code null}. It is guaranteed it exists as well.
     */
    Path userHome();

    /**
     * Returns the map of environment variables to be set before execution.
     * In case of conflict, the {@link Invocation#environmentVariables()} prevail.
     */
    Optional<Map<String, String>> environmentVariables();

    /**
     * Returns the builder with detected user home (Java {@code "user.home"} system property).
     */
    static Builder ofCurrentUserHome() {
        return ofUserHome(Path.of(System.getProperty("user.home")));
    }

    /**
     * Returns the builder with given user home.
     */
    static Builder ofUserHome(Path userHome) {
        return new Builder(userHome);
    }

    class Builder {
        private final Path userHome;
        private Map<String, String> environmentVariables;

        private Builder(Path userHome) {
            this.userHome = requireNonNull(userHome).toAbsolutePath().normalize();
            if (!Files.isDirectory(this.userHome)) {
                throw new IllegalArgumentException("userHome must be an existing directory");
            }
        }

        public Environment build() {
            return new Impl(userHome, environmentVariables);
        }

        public Builder withEnvironmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables = environmentVariables != null ? Map.copyOf(environmentVariables) : null;
            return this;
        }

        private static class Impl implements Environment {
            private final Path userHome;
            private final Map<String, String> environmentVariables;

            private Impl(Path userHome, Map<String, String> environmentVariables) {
                this.userHome = requireNonNull(userHome);
                this.environmentVariables = environmentVariables;
            }

            @Override
            public Path userHome() {
                return userHome;
            }

            @Override
            public Optional<Map<String, String>> environmentVariables() {
                return Optional.ofNullable(environmentVariables);
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Impl impl)) {
                    return false;
                }
                return Objects.equals(userHome, impl.userHome)
                        && Objects.equals(environmentVariables, impl.environmentVariables);
            }

            @Override
            public int hashCode() {
                return Objects.hash(userHome, environmentVariables);
            }
        }
    }
}
