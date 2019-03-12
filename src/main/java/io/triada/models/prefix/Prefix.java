package io.triada.models.prefix;

/**
 * Create prefix with fixed length
 */
public interface Prefix {

    int DEFAULT_LENGTH = 8;

    /**
     * @param length Prefix length
     * @return prefix
     */
    String create(int length);

    /**
     * @return Prefix length with default length (8)
     */
    default String create() {
        return this.create(DEFAULT_LENGTH);
    }
}
