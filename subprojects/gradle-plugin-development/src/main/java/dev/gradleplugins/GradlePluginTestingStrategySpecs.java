package dev.gradleplugins;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class GradlePluginTestingStrategySpecs {
    private GradlePluginTestingStrategySpecs() {}

    public static <T extends GradlePluginTestingStrategy> GradlePluginTestingStrategy.Spec<T> and(GradlePluginTestingStrategy.Spec<T> left, GradlePluginTestingStrategy.Spec<? super T> right) {
        return new AndSpec<>(left, right);
    }

    private static final class AndSpec<T extends GradlePluginTestingStrategy> extends GradlePluginTestingStrategy.Spec<T> {
        private final GradlePluginTestingStrategy.Spec<T> left;
        private final GradlePluginTestingStrategy.Spec<? super T> right;

        public AndSpec(GradlePluginTestingStrategy.Spec<T> left, GradlePluginTestingStrategy.Spec<? super T> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean isSatisfiedBy(T t) {
            return left.isSatisfiedBy(t) && right.isSatisfiedBy(t);
        }

        @Override
        void _do_not_extend_Spec___instead_uses_matches_factory_method() {}
    }

    public static <T extends GradlePluginTestingStrategy> GradlePluginTestingStrategy.Spec<T> or(GradlePluginTestingStrategy.Spec<T> left, GradlePluginTestingStrategy.Spec<? super T> right) {
        return new OrSpec<>(left, right);
    }

    private static final class OrSpec<T extends GradlePluginTestingStrategy> extends GradlePluginTestingStrategy.Spec<T> {
        private final GradlePluginTestingStrategy.Spec<T> left;
        private final GradlePluginTestingStrategy.Spec<? super T> right;

        public OrSpec(GradlePluginTestingStrategy.Spec<T> left, GradlePluginTestingStrategy.Spec<? super T> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean isSatisfiedBy(T t) {
            return left.isSatisfiedBy(t) || right.isSatisfiedBy(t);
        }

        @Override
        void _do_not_extend_Spec___instead_uses_matches_factory_method() {}
    }

    public static <T extends GradlePluginTestingStrategy> GradlePluginTestingStrategy.Spec<T> not(GradlePluginTestingStrategy.Spec<T> self) {
        return new NotSpec<>(self);
    }

    private static final class NotSpec<T extends GradlePluginTestingStrategy> extends GradlePluginTestingStrategy.Spec<T> {
        private final GradlePluginTestingStrategy.Spec<T> delegate;

        public NotSpec(GradlePluginTestingStrategy.Spec<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isSatisfiedBy(T t) {
            return !delegate.isSatisfiedBy(t);
        }

        @Override
        void _do_not_extend_Spec___instead_uses_matches_factory_method() {}
    }

    public static <T extends GradlePluginTestingStrategy> GradlePluginTestingStrategy.Spec<T> matches(Predicate<? super T> predicate) {
        return new MatchesSpec<>(predicate);
    }

    private static final class MatchesSpec<T extends GradlePluginTestingStrategy> extends GradlePluginTestingStrategy.Spec<T> {
        private final Predicate<? super T> predicate;

        public MatchesSpec(Predicate<? super T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean isSatisfiedBy(@Nullable T t) {
            if (t instanceof CompositeGradlePluginTestingStrategy) {
                return Stream.concat(Stream.of(t), StreamSupport.stream(((CompositeGradlePluginTestingStrategy) t).spliterator(), false)).anyMatch(it -> predicate.test((T) it));
            } else {
                return predicate.test(t);
            }
        }

        @Override
        void _do_not_extend_Spec___instead_uses_matches_factory_method() {}
    }
}
