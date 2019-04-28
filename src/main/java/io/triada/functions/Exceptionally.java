package io.triada.functions;

import lombok.experimental.UtilityClass;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedSupplier;

/**
 * Wrap exceptions to boolean representation
 */
@UtilityClass
public final class Exceptionally {

    /**
     * @param consumer Consumer
     * @param obj      Object to consume
     * @return True if exception was thrown
     */
    public boolean hasException(final CheckedConsumer<Object> consumer, final Object obj) {
        try {
            consumer.accept(obj);
            return false;
        } catch (final Throwable exc) {
            return true;
        }
    }

    /**
     * @param supplier Supplier
     * @return True if exception was thrown
     */
    public boolean hasException(final CheckedSupplier<Object> supplier) {
        try {
            supplier.get();
            return false;
        } catch (final Throwable exc) {
            return true;
        }
    }
}
