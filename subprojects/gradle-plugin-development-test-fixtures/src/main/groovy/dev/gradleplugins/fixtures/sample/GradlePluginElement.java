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

package dev.gradleplugins.fixtures.sample;

import dev.gradleplugins.fixtures.sources.SourceElement;

public abstract class GradlePluginElement extends SourceElement {
    public abstract String getPluginId();

    public abstract TestableGradlePluginElement withFunctionalTest();

    public TestableGradlePluginElement withTestKitTest() {
        return new TestableGradlePluginElement(this, new BasicGradlePluginTestKitTest("test"));
    }

    public TestableGradlePluginElement withTestKitFunctionalTest() {
        return new TestableGradlePluginElement(this, new BasicGradlePluginTestKitTest("functionalTest"));
    }

    public TestableGradlePluginElement withProjectBuilderTest() {
        return new TestableGradlePluginElement(this, new BasicGradlePluginProjectBuilderTest());
    }
}
