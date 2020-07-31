/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.Duration;

public final class RetryUtil {
    private RetryUtil() {}

    // TODO: Maybe Runnable is not a good fit here as it can throw exception from within...
    public static int retry(Runnable closure) throws InterruptedException {
        return retry(3, closure);
    }

    public static int retry(int retries, Runnable closure) throws InterruptedException {
        return retry(retries, 0, closure);
    }

    @Deprecated
    public static int retry(int retries, int waitMsBetweenRetries, Runnable closure) throws InterruptedException {
        return retry(retries, Duration.ofMillis(waitMsBetweenRetries), closure);
    }

    public static int retry(int retries, Duration waitBetweenRetries, Runnable closure) throws InterruptedException {
        int retryCount = 0;
        Throwable lastException = null;

        while (retryCount++ < retries) {
            try {
                closure.run();
                return retryCount;
            } catch (Throwable e) {
                lastException = e;
                Thread.sleep(waitBetweenRetries.toMillis());
            }
        }

        // Retry count exceeded, throwing last exception
        return ExceptionUtils.rethrow(lastException);
    }
}
