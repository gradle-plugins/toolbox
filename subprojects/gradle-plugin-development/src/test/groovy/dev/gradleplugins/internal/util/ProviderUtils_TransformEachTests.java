package dev.gradleplugins.internal.util;

import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.ProviderUtils.transformEach;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ProviderUtils_TransformEachTests {
    Provider<List<String>> subject = providerFactory().provider(() -> Arrays.asList("first", "second", "third"));
    @Mock Transformer<String, String> transformer;

    @Test
    void transformEachElementsWhenProviderRealized() {
        Mockito.when(transformer.transform(any())).thenAnswer(args -> "transformed-" + args.getArgument(0, String.class));
        assertThat(subject.map(transformEach(transformer)), providerOf(contains("transformed-first", "transformed-second", "transformed-third")));
    }

    @Test
    void doesNotTransformEachElementsWhenProviderNotRealized() {
        subject.map(transformEach(transformer));
        Mockito.verifyNoInteractions(transformer);
    }

    private static ProviderFactory providerFactory() {
        return ProjectBuilder.builder().build().getProviders();
    }
}
