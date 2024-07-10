package dev.gradleplugins.internal.dsl.groovy

import dev.gradleplugins.internal.runtime.dsl.GroovyHelper

/**
 * Helper class to add extension methods to Groovy DSL classes at runtime.
 * The end result is comparable to the Kotlin extension methods.
 */
class GroovyDslRuntimeExtensions extends GroovyHelper {
    /**
     * Add an extension methods to an object.
     *
     * @param self the object to extend
     * @param methodName the extension method name
     * @param methodBody the extension method body
     */
    @Override
    void addNewInstanceMethod(Object self, String methodName, Closure methodBody) {
        self.metaClass."${methodName}" = methodBody
    }
}
