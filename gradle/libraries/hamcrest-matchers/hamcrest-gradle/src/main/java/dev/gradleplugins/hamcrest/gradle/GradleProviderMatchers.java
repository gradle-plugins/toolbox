/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.hamcrest.gradle;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.regex.Pattern;

public final class GradleProviderMatchers {
	private GradleProviderMatchers() {}

	/**
	 * Matches any exception message variant thrown when disallowed change applied to Gradle property.
	 *
	 * @return  an exception message matcher, never null.
	 */
	public static Matcher<String> forChangesDisallowed() {
		return forChangesDisallowed(ON_ANY_TARGET);
	}

	private static final PropertyTarget ON_ANY_TARGET = new PropertyTarget() {
		@Override
		public String toString() {
			return ".+";
		}
	};

	public static Matcher<String> forChangesDisallowed(PropertyTarget target) {
		return Matchers.matchesPattern(exceptionMessagePattern(target.toString()));
	}

	public interface PropertyTarget {
		static PropertyTargetBuilder ofOwner(Object toString) {
			return new PropertyTargetBuilder() {
				@Override
				public PropertyTarget property(String name) {
					return new PropertyTarget() {
						@Override
						public String toString() {
							return toString + " property '" + name + "'";
						}
					};
				}
			};
		}

		static PropertyTarget onProperty(String name) {
			return new PropertyTarget() {
				@Override
				public String toString() {
					return ".*property '" + name + "'";
				}
			};
		}

		interface PropertyTargetBuilder extends PropertyTarget {
			PropertyTarget property(String name);
		}
	}

	// Matches message such as:
	//   - The value for this property cannot be changed any further.
	//   - The value for property 'foo' cannot be changed any further.
	//   - The value for this file collection cannot be changed.
	// Note that we implicitly check for finalized value as final value imply disallowed changes.
	// Notice the difference in the exception message for _file collection_.
	// Sample "The value for test suite 'functionalTest' property 'sourceSet' is final and cannot be changed any further."
	//                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^                  ^^^^^^^^^^^^
	//                              owner toString        prop display name     if final                     prop vs FC
	//
	// Sample "The value for property 'testedSourceSet' cannot be changed any further."
	//                       ^^^^^^^^^^^^^^^^^^^^^^^^^^                  ^^^^^^^^^^^^
	//                           prop display name                        prop vs FC
	private static Pattern exceptionMessagePattern(String targetPattern) {
		return Pattern.compile("The value for " + targetPattern + " (is final and )?cannot be changed( any further)?.");
	}
}
