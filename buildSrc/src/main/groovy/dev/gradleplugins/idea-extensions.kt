///*
// * Copyright 2019 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package dev.gradleplugins
//
//import org.gradle.api.plugins.ExtensionAware
//import org.gradle.kotlin.dsl.configure
//import org.gradle.plugins.ide.idea.model.IdeaProject
//import org.jetbrains.gradle.ext.CopyrightConfiguration
//import org.jetbrains.gradle.ext.ProjectSettings
//
//fun IdeaProject.settings(configuration: ProjectSettings.() -> kotlin.Unit) = (this as ExtensionAware).configure(configuration)
//
//fun ProjectSettings.copyright(configuration: CopyrightConfiguration.() -> kotlin.Unit) = (this as ExtensionAware).configure(configuration)