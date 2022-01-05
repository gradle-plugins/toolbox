package dev.gradleplugins;

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionsSchema;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;

public final class ProjectMatchers {
    public static <T> Matcher<T> extensions(Matcher<? super Iterable<ExtensionsSchema.ExtensionSchema>> matcher) {
        return new FeatureMatcher<T, Iterable<ExtensionsSchema.ExtensionSchema>>(matcher, "", "") {
            @Override
            protected Iterable<ExtensionsSchema.ExtensionSchema> featureValueOf(T actual) {
                return ((ExtensionAware) actual).getExtensions().getExtensionsSchema();
            }
        };
    }

    public static <T> Matcher<T> named(String name) {
        return new FeatureMatcher<T, String>(equalTo(name), "", "") {
            @Override
            protected String featureValueOf(T actual) {
                if (actual instanceof Named) {
                    return ((Named) actual).getName();
                } else if (actual instanceof NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) {
                    return ((NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) actual).getName();
                }
                return null;
//                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> Matcher<T> publicType(Class<?> type) {
        return new FeatureMatcher<T, TypeOf<?>>(equalTo(TypeOf.typeOf(type)), "", "") {
            @Override
            protected TypeOf<?> featureValueOf(T actual) {
                if (actual instanceof HasPublicType) {
                    return ((HasPublicType) actual).getPublicType();
                } else if (actual instanceof NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) {
                    return ((NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) actual).getPublicType();
                }
                throw new UnsupportedOperationException();
            }
        };
    }
}