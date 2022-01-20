package dev.gradleplugins;

import org.gradle.api.Named;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A testing strategy for a Gradle plugin project test suite.
 *
 * @see GradlePluginDevelopmentTestSuite
 */
public interface GradlePluginTestingStrategy extends Named {
    /**
     * Type-safe accessor to the {@literal testingStrategy} extension on {@code Test} task.
     *
     * @param task  the test task, must not be null
     * @return a {@code Provider} for the testing strategy of the specified {@code Test} task.
     * @throws UnknownDomainObjectException when {@literal testingStrategy} extension is not present
     */
    @SuppressWarnings("unchecked")
    static Provider<GradlePluginTestingStrategy> testingStrategy(Test task) throws UnknownDomainObjectException {
        return (Provider<GradlePluginTestingStrategy>) task.getExtensions().getByName("testingStrategy");
    }

    /**
     * Represents specification for {@link GradlePluginTestingStrategy}.
     * The specification will walk through each element of a composite testing strategy as well as itself when testing the satisfaction.
     *
     * @param <T> testing strategy type
     */
    abstract class Spec<T extends GradlePluginTestingStrategy> {
        public abstract boolean isSatisfiedBy(@Nullable T t);

        /**
         * This method simply acts a friendly reminder not to extends Spec directly and instead uses {@link #matches(Predicate)} factory method.
         * It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .
         *
         * @deprecated to make
         */
        @Deprecated
        abstract void _do_not_extend_Spec___instead_uses_matches_factory_method();

        /**
         * Returns a specification that intersect between this spec and the specified spec.
         *
         * @param spec  a intersect spec, must not be null
         * @return a intersect specification, never null
         */
        public Spec<T> and(Spec<? super T> spec) {
            Objects.requireNonNull(spec);
            return GradlePluginTestingStrategySpecs.and(this, spec);
        }

        /**
         * Returns a specification that union between this spec and the specified spec.
         *
         * @param spec  a union spec, must not be null
         * @return a union specification, never null
         */
        public Spec<T> or(Spec<? super T> spec) {
            Objects.requireNonNull(spec);
            return GradlePluginTestingStrategySpecs.or(this, spec);
        }

        /**
         * Returns a specification that negate this spec.
         *
         * @return a negation specification, never null
         */
        public Spec<T> negate() {
            return GradlePluginTestingStrategySpecs.not(this);
        }

        /**
         * Returns a specification that try to match any testing strategy with the specified predicate.
         *
         * @param predicate  a predicate to match any testing strategy, must not be null
         * @param <T>  testing strategy type to satisfy
         * @return a specification that matches any testing strategy with the specified predicate, never null
         */
        public static <T extends GradlePluginTestingStrategy> Spec<T> matches(Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate);
            return GradlePluginTestingStrategySpecs.matches(predicate);
        }
    }
}
