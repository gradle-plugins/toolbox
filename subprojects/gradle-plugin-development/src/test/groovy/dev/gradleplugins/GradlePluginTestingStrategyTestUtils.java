package dev.gradleplugins;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class GradlePluginTestingStrategyTestUtils {
    private GradlePluginTestingStrategyTestUtils() {}

    public static GradlePluginTestingStrategy aStrategy() {
        return new AStrategy();
    }

    private static final class AStrategy implements GradlePluginTestingStrategy {
        @Override
        public String getName() {
            return "aStrategy";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o.getClass().equals(AStrategy.class);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName());
        }

        @Override
        public String toString() {
            return "aStrategy()";
        }
    }

    public static GradlePluginTestingStrategy anotherStrategy() {
        return new AnotherStrategy(null);
    }

    public static GradlePluginTestingStrategy anotherStrategy(Object what) {
        return new AnotherStrategy(Objects.requireNonNull(what));
    }

    private static final class AnotherStrategy implements GradlePluginTestingStrategy {
        @Nullable private final Object what;

        private AnotherStrategy(@Nullable Object what) {
            this.what = what;
        }

        @Override
        public String getName() {
            return Optional.ofNullable(what).map(Object::toString).orElse("anotherStrategy");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AnotherStrategy)) {
                return false;
            }
            AnotherStrategy that = (AnotherStrategy) o;
            return Objects.equals(what, that.what);
        }

        @Override
        public int hashCode() {
            return Objects.hash(what);
        }

        @Override
        public String toString() {
            return "anotherStrategy(" + (what == null ? "" : what.toString()) + ")";
        }
    }

    public static GradlePluginTestingStrategy aCompositeStrategy(GradlePluginTestingStrategy... strategies) {
        return new ACompositeStrategy(Arrays.asList(strategies));
    }

    private static final class ACompositeStrategy implements CompositeGradlePluginTestingStrategy {
        private final Iterable<GradlePluginTestingStrategy> strategies;

        private ACompositeStrategy(Iterable<GradlePluginTestingStrategy> strategies) {
            this.strategies = strategies;
        }

        @Override
        public String getName() {
            return StringUtils.uncapitalize(StreamSupport.stream(strategies.spliterator(), false).map(GradlePluginTestingStrategy::getName).map(StringUtils::capitalize).collect(Collectors.joining()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ACompositeStrategy)) {
                return false;
            }
            ACompositeStrategy that = (ACompositeStrategy) o;
            return Objects.equals(strategies, that.strategies);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strategies);
        }

        @Override
        public String toString() {
            return "aCompositeStrategy(" + strategies + ")";
        }

        @Override
        public Iterator<GradlePluginTestingStrategy> iterator() {
            return strategies.iterator();
        }
    }
}
