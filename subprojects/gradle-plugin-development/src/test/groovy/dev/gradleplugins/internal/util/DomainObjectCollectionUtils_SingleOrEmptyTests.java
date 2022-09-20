package dev.gradleplugins.internal.util;

import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.DomainObjectCollectionUtils.singleOrEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class DomainObjectCollectionUtils_SingleOrEmptyTests {

    @Test
    void returnsEmptyIterableForAbsentProvider() {
        assertThat(singleOrEmpty(providerFactory().provider(() -> null)), providerOf(emptyIterable()));
    }

    @Test
    void returnsIterableWithSingleElementForPresentProvider() {
        assertThat(singleOrEmpty(providerFactory().provider(() -> "single")), providerOf(contains("single")));
    }

    private static ProviderFactory providerFactory() {
        return ProjectBuilder.builder().build().getProviders();
    }
}
