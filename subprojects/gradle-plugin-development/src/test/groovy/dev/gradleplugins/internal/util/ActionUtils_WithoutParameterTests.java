package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.gradleplugins.internal.util.ActionUtils.withoutParameter;

@ExtendWith(MockitoExtension.class)
class ActionUtils_WithoutParameterTests {
    @Mock Runnable runnable;
    Action<Object> subject;

    @BeforeEach
    void createSubject() {
        subject = withoutParameter(runnable);
    }

    @Test
    void callsRunnableOnActionExecution() {
        subject.execute(new Object());
        Mockito.verify(runnable).run();
    }
}
