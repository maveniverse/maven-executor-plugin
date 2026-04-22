package eu.maveniverse.maven.executor.core;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The invocation.
 */
public interface Invocation {
    /**
     * The command.
     */
    String cmd();

    /**
     * The arguments.
     */
    List<String> args();

    /**
     * Returns the map of environment variables to be set before execution.
     * Takes precedence over {@link Environment#environmentVariables()}.
     */
    Optional<Map<String, String>> environmentVariables();

    /**
     * Returns the builder for Maven command.
     */
    static Builder ofMvn() {
        return ofCmd("mvn");
    }

    /**
     * Returns the builder for given command.
     */
    static Builder ofCmd(String cmd) {
        return new Builder(cmd);
    }

    class Builder {
        private final String cmd;
        private List<String> args;
        private Map<String, String> environmentVariables;

        private Builder(String cmd) {
            this.cmd = requireNonNull(cmd);
            this.args = List.of();
        }

        public Invocation build() {
            return new Impl(cmd, args, environmentVariables);
        }

        public Builder withArgs(List<String> args) {
            this.args = List.copyOf(args);
            return this;
        }

        public Builder withArgs(String... args) {
            this.args = List.copyOf(Arrays.asList(args));
            return this;
        }

        public Builder withEnvironmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables = environmentVariables != null ? Map.copyOf(environmentVariables) : null;
            return this;
        }

        private static class Impl implements Invocation {
            private final String cmd;
            private final List<String> args;
            private final Map<String, String> environmentVariables;

            private Impl(String cmd, List<String> args, Map<String, String> environmentVariables) {
                this.cmd = requireNonNull(cmd);
                this.args = requireNonNull(args);
                this.environmentVariables = environmentVariables;
            }

            @Override
            public String cmd() {
                return cmd;
            }

            @Override
            public List<String> args() {
                return args;
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
                return Objects.equals(cmd, impl.cmd)
                        && Objects.equals(args, impl.args)
                        && Objects.equals(environmentVariables, impl.environmentVariables);
            }

            @Override
            public int hashCode() {
                return Objects.hash(cmd, args, environmentVariables);
            }
        }
    }
}
