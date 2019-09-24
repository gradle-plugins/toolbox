/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.license.internal;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.plugins.ide.idea.IdeaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class OpenSourceSoftwareLicensePlugin implements Plugin<Project> {
    public void apply(Project project) {
        OpenSourceSoftwareLicenseExtension ossLicense = project.getExtensions().create("ossLicense", OpenSourceSoftwareLicenseExtension.class);

        project.getPluginManager().withPlugin("maven-publish", appliedPlugin -> {
            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            publishing.getPublications().withType(MavenPublication.class, publication -> {
                publication.pom(pom -> {
                    pom.licenses(licenses -> {
                        licenses.license(license -> {
                            license.getName().set(ossLicense.getDisplayName());
                            license.getUrl().set(ossLicense.getLicenseUrl().map(URL::toString));
                            license.getDistribution().set("repo");
                        });
                    });
                });
            });
        });

        project.getPluginManager().withPlugin("com.jfrog.bintray", appliedPlugin -> {
            // TODO: Remove once bintray support Provider API
            project.afterEvaluate(evaluatedProject -> {
                try {
                    Class extensionClass = Class.forName("com.jfrog.bintray.gradle.BintrayExtension");
                    Object extension = evaluatedProject.getExtensions().getByType(extensionClass);
                    Object pkg = get(extension, "getPkg");

                    set(pkg, "setLicenses", ossLicense.getName().get());
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        });

        project.getPluginManager().withPlugin("org.jetbrains.gradle.plugin.idea-ext", appliedPlugin -> {
            project.getPlugins().withType(IdeaPlugin.class, plugin -> {
                try {
                    Class settingsExtensionClass = Class.forName("org.jetbrains.gradle.ext.ProjectSettings");
                    Object settingsExtension = ((ExtensionAware) plugin.getModel().getProject()).getExtensions().getByType(settingsExtensionClass);

                    Class copyrightExtensionClass = Class.forName("org.jetbrains.gradle.ext.CopyrightConfiguration");
                    Object copyrightExtension = ((ExtensionAware) settingsExtension).getExtensions().getByType(copyrightExtensionClass);

                    set(copyrightExtension, "setUseDefault", ossLicense.getShortName().get());
                    NamedDomainObjectContainer<Object> profiles = (NamedDomainObjectContainer<Object>)get(copyrightExtension, "getProfiles");
                    Object profile = profiles.create(ossLicense.getShortName().get());
                    set(profile, "setNotice", ossLicense.getCopyrightFileHeader().get());
                    set(profile, "setKeyword", "Copyright");
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        });

//        plugins.withType<SetupProjectPlugin> {
//            // TODO: Only in rootProject
//            if (isRootProject()) {
//                val generateLicenseFileTask = tasks.register("generateLicenseFile") {
//                    doLast {
//                        file("LICENSE").writeText(ossLicense.licenseUrl.get().readText())
//                    }
//                }
//
//                tasks.named("setup") {
//                    dependsOn(generateLicenseFileTask)
//                }
//            }
//        }
    }

    private static Object get(Object instance, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return instance.getClass().getMethod(methodName).invoke(instance);
    }

    private static Object set(Object instance, String methodName, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return instance.getClass().getMethod(methodName, value.getClass()).invoke(instance, value);
    }
}
