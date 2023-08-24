package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.GradleDsl;
import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleRunner;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public interface DependencyWiringTester {
    ExpressionBuilder<?> bucketDsl();

    GradleBuildFile buildFile();

    GradleRunner runner();

    default void assertBucketDependencyIs(Expression expression) {
        buildFile().append(bucketDsl().call("add", string("com.example:foo:1.0")));
        buildFile().append(groovyDsl(
                "Set<String> allDependencies(Configuration configuration) {",
                "  return configuration.allDependencies.collect {",
                "    if (it instanceof ExternalModuleDependency) {",
                "      return \"${it.group}:${it.name}:${it.version}\".toString()",
                "    } else if (it instanceof ProjectDependency) {",
                "      return it.projectPath",
                "    } else if (it instanceof SelfResolvingDependency) {",
                "      return it.targetComponentId?.displayName ?: '<no-display-name>'",
                "    } else {",
                "      throw new RuntimeException()",
                "    }",
                "  }",
                "}"
        ));
        buildFile().append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert " + expression.toString(GradleDsl.GROOVY),
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }

    default Expression containedIn(Expression configurationName) {
        return groovyDsl("allDependencies(configurations[" + configurationName.toString(GradleDsl.GROOVY) + "]).contains('com.example:foo:1.0')");
    }
}
