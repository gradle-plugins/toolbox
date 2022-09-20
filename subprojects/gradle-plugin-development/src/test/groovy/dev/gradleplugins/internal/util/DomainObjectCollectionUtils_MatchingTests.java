package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.gradleplugins.internal.util.DomainObjectCollectionUtils.matching;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DomainObjectCollectionUtils_MatchingTests {
    @Mock Spec<Object> spec;
    @Mock Action<Object> delegate;
    Action<Object> subject;

    @BeforeEach
    void createSubject() {
        subject = matching(spec, delegate);
    }

    @Test
    void callsDelegateActionWhenSpecIsSatisfied() {
        final Object value = new Object();
        Mockito.when(spec.isSatisfiedBy(value)).thenReturn(true);
        subject.execute(value);
        Mockito.verify(delegate).execute(value);
    }

    @Test
    void doesNotCallsDelegateActionWhenSpecIsNotSatisfied() {
        final Object value = new Object();
        Mockito.when(spec.isSatisfiedBy(value)).thenReturn(false);
        subject.execute(value);
        Mockito.verify(delegate, never()).execute(value);
    }
}
