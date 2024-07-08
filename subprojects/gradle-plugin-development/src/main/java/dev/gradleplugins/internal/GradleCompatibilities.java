package dev.gradleplugins.internal;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

import static dev.gradleplugins.GradlePluginDevelopmentDependencyExtension.GRADLE_API_LOCAL_VERSION;

public abstract class GradleCompatibilities {
    public abstract Property<String> getMinimumGradleVersion();
    public abstract Property<String> getGradleApiVersion();

    /*private*/ static abstract /*final*/ class Rule implements org.gradle.api.Plugin<Project> {
        @Inject
        public Rule() {}

        @Override
        public void apply(Project project) {
            final GradleCompatibilitiesExtension extension = project.getExtensions().create("$gradleCompatibilities", GradleCompatibilitiesExtension.class);
            extension.configureEach(new Action<GradleCompatibilities>() {
                @Override
                public void execute(GradleCompatibilities it) {
                    // For now, we will not allow to finalize the value just yet because of the override for 1.x series
                    //   See GradlePluginDevelopmentCompatibilityExtensionRule
                    //it.getMinimumGradleVersion().finalizeValueOnRead();

                    it.getGradleApiVersion().convention(it.getMinimumGradleVersion().map(toLocalIfGradleSnapshotVersion()));
                    it.getGradleApiVersion().finalizeValueOnRead();
                }

                private /*static*/ Transformer<String, String> toLocalIfGradleSnapshotVersion() {
                    return it -> {
                        if (GradleVersion.version(it).isSnapshot()) {
                            return GRADLE_API_LOCAL_VERSION;
                        }
                        return it;
                    };
                }
            });
        }
    }

    public interface ForSourceSetExtension {
        NamedDomainObjectProvider<SourceSetGradleCompatibilities> forSourceSet(SourceSet sourceSet);
        void configureEach(Action<? super SourceSetGradleCompatibilities> action);

        abstract class SourceSetGradleCompatibilities extends GradleCompatibilities {
            public abstract SourceSet getSourceSet();
        }
    }

    /*private*/ static abstract /*final*/ class GradleCompatibilitiesExtension implements ForSourceSetExtension {
        private final ObjectFactory objects;
        private final NamedDomainObjectSet<SourceSetGradleCompatibilities> compatibilities;

        @Inject
        public GradleCompatibilitiesExtension(ObjectFactory objects) {
            this.objects = objects;
            this.compatibilities = objects.namedDomainObjectSet(SourceSetGradleCompatibilities.class);
        }

        public NamedDomainObjectProvider<SourceSetGradleCompatibilities> forSourceSet(SourceSet sourceSet) {
            if (compatibilities.findByName(sourceSet.getName()) == null) {
                compatibilities.add(objects.newInstance(DefaultGradleCompatibility.class, sourceSet));
            }
            return compatibilities.named(sourceSet.getName());
        }

        @Override
        public void configureEach(Action<? super SourceSetGradleCompatibilities> action) {
            compatibilities.configureEach(action);
        }
    }

    /*private*/ static abstract /*final*/ class DefaultGradleCompatibility extends ForSourceSetExtension.SourceSetGradleCompatibilities implements Named {
        private final SourceSet sourceSet;

        @Inject
        public DefaultGradleCompatibility(SourceSet sourceSet) {
            this.sourceSet = sourceSet;
        }

        @Override
        public String getName() {
            return sourceSet.getName();
        }

        @Override
        public SourceSet getSourceSet() {
            return sourceSet;
        }
    }
}
