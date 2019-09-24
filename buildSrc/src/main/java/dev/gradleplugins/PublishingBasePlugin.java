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

package dev.gradleplugins;

import com.jfrog.bintray.gradle.BintrayExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PublishingBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("com.jfrog.bintray", appliedPlugin -> {
            project.afterEvaluate(evaluatedProject -> {
                BintrayExtension bintray = project.getExtensions().getByType(BintrayExtension.class);
                bintray.setUser(resolveProperty(project, "BINTRAY_USER", "dev.gradleplugins.bintrayUser"));
                bintray.setKey(resolveProperty(project, "BINTRAY_KEY", "dev.gradleplugins.bintrayKey"));
                bintray.setPublish(true);

                bintray.getPkg().setRepo("maven" + (project.getVersion().toString().contains("-SNAPSHOT") ? "-snapshot" : ""));
                bintray.getPkg().setDesc(project.getDescription());

                bintray.getPkg().getVersion().setReleased(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").format(new Date()));
                bintray.getPkg().getVersion().getGpg().setSign(false);
                bintray.getPkg().getVersion().getGpg().setPassphrase(resolveProperty(project, "GPG_PASSPHRASE", "gpgPassphrase"));
            });
        });
    }


    @Nullable
    private String resolveProperty(Project project, String envVarKey, String projectPropKey) {
        Object propValue = System.getenv().get(envVarKey);

        if (propValue != null) {
            return propValue.toString();
        }

        propValue = project.findProperty(projectPropKey);
        if (propValue != null) {
            return propValue.toString();
        }

        return null;
    }
}
