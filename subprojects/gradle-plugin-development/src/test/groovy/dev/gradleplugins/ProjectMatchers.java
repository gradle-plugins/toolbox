package dev.gradleplugins;

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionsSchema;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.Matchers.equalTo;

public final class ProjectMatchers {
    public static <T> Matcher<T> extensions(Matcher<? super Iterable<ExtensionsSchema.ExtensionSchema>> matcher) {
        return new FeatureMatcher<T, Iterable<ExtensionsSchema.ExtensionSchema>>(matcher, "a extension aware object", "extension aware object") {
            @Override
            protected Iterable<ExtensionsSchema.ExtensionSchema> featureValueOf(T actual) {
                return ((ExtensionAware) actual).getExtensions().getExtensionsSchema().getElements();
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
                } else if (actual instanceof ArtifactRepository) {
                    return ((ArtifactRepository) actual).getName();
                } else if (actual instanceof SourceSet) {
                    return ((SourceSet) actual).getName();
                } else if (actual instanceof Task) {
                    return ((Task) actual).getName();
                } else if (actual instanceof Configuration) {
                    return ((Configuration) actual).getName();
                }
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> Matcher<T> publicType(Class<?> type) {
        return publicType(TypeOf.typeOf(type));
    }

    public static <T> Matcher<T> publicType(TypeOf<?> type) {
        return new FeatureMatcher<T, TypeOf<?>>(equalTo(type), "", "") {
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

    public static <T> Matcher<Provider<? extends T>> providerOf(T instance) {
        return providerOf(equalTo(instance));
    }

    public static <T> Matcher<Provider<? extends T>> providerOf(Matcher<? super T> matcher) {
        return new FeatureMatcher<Provider<? extends T>, T>(matcher, "", "") {
            @Override
            protected T featureValueOf(Provider<? extends T> actual) {
                return actual.get();
            }
        };
    }

    public static <T> Matcher<Provider<? extends T>> absentProvider() {
        return new TypeSafeMatcher<Provider<? extends T>>() {
            @Override
            protected boolean matchesSafely(Provider<? extends T> item) {
                return !item.isPresent();
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    public static <T> Matcher<Provider<? extends T>> presentProvider() {
        return new TypeSafeMatcher<Provider<? extends T>>() {
            @Override
            protected boolean matchesSafely(Provider<? extends T> item) {
                return item.isPresent();
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    public static Matcher<PluginAware> hasPlugin(String pluginId) {
        return new TypeSafeMatcher<PluginAware>() {
            @Override
            protected boolean matchesSafely(PluginAware item) {
                return item.getPluginManager().hasPlugin(pluginId);
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    public static <T> Matcher<T> coordinate(String coordinate) {
        return new FeatureMatcher<T, String>(equalTo(coordinate), "", "") {
            @Override
            protected String featureValueOf(T actual) {
                Dependency dependency = null;
                if (actual instanceof Dependency) {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(((Dependency) actual).getGroup());
                    builder.append(":").append(((Dependency) actual).getName());
                    builder.append(":").append(((Dependency) actual).getVersion());
                    return builder.toString();
                } else if (actual instanceof Capability) {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(((Capability) actual).getGroup());
                    builder.append(":").append(((Capability) actual).getName());
                    builder.append(":").append(((Capability) actual).getVersion());
                    return builder.toString();
                }
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> Matcher<T> capabilities(Matcher<? super Iterable<Capability>> matcher) {
        return new FeatureMatcher<T, Iterable<Capability>>(matcher, "", "") {
            @Override
            protected Iterable<Capability> featureValueOf(T actual) {
                if (actual instanceof Dependency) {
                    return ((ModuleDependency) actual).getRequestedCapabilities();
                } else  if (actual instanceof ConfigurationPublications) {
                    @SuppressWarnings("unchecked")
                    final Iterable<Capability> result = (Iterable<Capability>) ((ConfigurationPublications) actual).getCapabilities();
                    return result;
                }
                throw new UnsupportedOperationException();
            }
        };
    }

    public static Matcher<Task> shouldRunAfter(Matcher<? super Iterable<? extends Task>> matcher) {
        return new FeatureMatcher<Task, Iterable<? extends Task>>(matcher, "", "") {
            @Override
            protected Iterable<? extends Task> featureValueOf(Task actual) {
                return actual.getShouldRunAfter().getDependencies(null);
            }
        };
    }
}
