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

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.net.URI;
import java.net.URL;

public abstract class GitHubSourceControlManagerExtension {
    public enum SourceControlManagerProtocol {
        HTTPS, GIT
    }

    public abstract Property<String> getGitHubHostName();
    public abstract Property<String> getGitHubOrganization();
    public abstract Property<String> getGitHubRepositoryName();

    public Provider<URL> getGitHubWebsiteUrl() {
        return getProviderFactory().provider(() -> new URL("https://" + getGitHubHostName().get() + "/" + getGitHubRepositorySlug().get()));
    }

    public Provider<URL> getGitHubIssueTrackerUrl() {
        return getProviderFactory().provider(() -> new URL("https://" + getGitHubHostName().get() + "/" + getGitHubRepositorySlug().get() + "/issues"));
    }

    public Provider<URI> sourceControlManagerUrl(SourceControlManagerProtocol protocol) {
        switch (protocol) {
            case HTTPS: return getProviderFactory().provider(() -> new URI("https://" + getGitHubHostName().get() + "/" + getGitHubRepositorySlug().get() + ".git"));
            case GIT: return getProviderFactory().provider(() -> new URI("git://" + getGitHubHostName().get() + "/" + getGitHubRepositorySlug().get() + ".git"));
        }

        throw new IllegalArgumentException("Invalid SCM protocol");
    }


    public Provider<String> getGitHubRepositorySlug() {
        return getProviderFactory().provider(() -> getGitHubOrganization().get() + "/" + getGitHubRepositoryName().get());
    }

    @Inject
    protected ProviderFactory getProviderFactory() {
        throw new UnsupportedOperationException();
    }
}
