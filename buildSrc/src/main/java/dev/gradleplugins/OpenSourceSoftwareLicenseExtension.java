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

import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.net.URL;

public class OpenSourceSoftwareLicenseExtension {
    public Provider<String> getDisplayName() {
        return getProviderFactory().provider(() -> "The Apache Software License, Version 2.0");
    }

    public Provider<URL> getLicenseUrl() {
        return getProviderFactory().provider(() -> new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"));
    }

    public Provider<String> getName() {
        return getProviderFactory().provider(() -> "Apache-2.0");
    }

    public Provider<String> getShortName() {
        return getProviderFactory().provider(() -> "ASL2");
    }

    public Provider<String> getCopyrightFileHeader() {
        return getProviderFactory().provider(() -> "Copyright $today.year the original author or authors.\n"
                + "\n"
                + "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
                + "you may not use this file except in compliance with the License.\n"
                + "You may obtain a copy of the License at\n"
                + "\n"
                + "     https://www.apache.org/licenses/LICENSE-2.0\n"
                + "\n"
                + "Unless required by applicable law or agreed to in writing, software\n"
                + "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
                + "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
                + "See the License for the specific language governing permissions and\n"
                + "limitations under the License.");
    }

    @Inject
    protected ProviderFactory getProviderFactory() {
        throw new UnsupportedOperationException();
    }
}
