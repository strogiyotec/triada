package io.triada.models.threads;

import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public final class Sleep {
    public void withDuration(final TimeUnit timeUnit, final long amount) {
        try {
            timeUnit.sleep(amount);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
