package dev.gradleplugins.fixtures;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import java.util.Collections;

public abstract class WellBehavedPluginTest {
    private Project project = ProjectBuilder.builder().build();

    protected abstract String getQualifiedPluginId();

    protected abstract Class<? extends Plugin<Project>> getPluginType();

    @Test
    public void can_apply_plugin_using_qualified_plugin_id() {
        // when:
        project.apply(Collections.singletonMap("plugin", getQualifiedPluginId()));

        // then:
        // noExceptionThrown()
    }

    @Test
    public void can_apply_plugin_using_class() {
        // when:
        project.apply(Collections.singletonMap("plugin", getPluginType()));

        // then:
        // noExceptionThrown()
    }
}
