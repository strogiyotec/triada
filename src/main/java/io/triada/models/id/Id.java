package io.triada.models.id;

import io.triada.text.Text;

/**
 * Represent id of wallet
 * @param <T> type
 */
public interface Id<T> extends Text{

    /**
     *
     * @return id
     */
    T id();
}
