/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.gradleplugins.test.fixtures.file.TestFile;
import lombok.Value;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.net.URL;

public abstract class DownloadableGradleDistribution extends DefaultGradleDistribution {
    private static final LoadingCache<Key, File> CACHE = createCache();

    private static LoadingCache<Key, File> createCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build(
                        new CacheLoader<Key, File>() {
                            public File load(Key key) {
                                System.out.println("downloading " + key.url.toString());
                                key.getBinDistribution().copyFrom(key.url);
                                key.getBinDistribution().usingNativeTools().unzipTo(key.versionDir);
                                return key.getBinDistribution();
                            }
                        });
    }

    @Value
    private static class Key {
        URL url;
        TestFile binDistribution;
        TestFile versionDir;
    }

    protected TestFile versionDir;

    public DownloadableGradleDistribution(String version, TestFile versionDir) {
        super(GradleVersion.version(version), versionDir.file("gradle-$version"), versionDir.file("gradle-$version-bin.zip"));
        this.versionDir = versionDir;
    }

    public TestFile getBinDistribution() {
        download();
        return super.getBinDistribution();
    }

    public TestFile getGradleHomeDirectory() {
        download();
        return super.getGradleHomeDirectory();
    }

    private void download() {
        CACHE.getUnchecked(new Key(getDownloadURL(), super.getBinDistribution(), versionDirectory));

        super.getBinDistribution().assertIsFile();
        super.getGradleHomeDirectory().assertIsDirectory();
    }

    protected abstract URL getDownloadURL();
}
