package io.triada.models.hash;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public interface Hash {
    /**
     * @return Txn hash
     */
    String hash();

    /**
     * @return Txn nonce
     */
    String nonce();

    static String sha256(final String first, final String second) {
        return Hashing.sha256().hashString(
                String.format(
                        "%s %s",
                        first,
                        second
                ),
                StandardCharsets.UTF_8
        ).toString();
    }
}