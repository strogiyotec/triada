package io.triada.models.hash;

import com.google.common.hash.Hashing;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Calculate hash of transaction
 */
public final class CalculatedHash extends HashEnvelope {

    public CalculatedHash(final String prefix, final int strength) {
        super(
                () -> {
                    BigInteger nonce = BigInteger.ONE;
                    while (true) {
                        nonce = nonce.add(BigInteger.ONE);
                        final String hash = Hashing.sha256().hashString(prefix + " " + nonce.toString(), StandardCharsets.UTF_8).toString();
                        if (checkHash(hash, strength)) {
                            return new ConstTxnHash(hash, nonce);
                        }
                    }
                }
        );
    }

    private static boolean checkHash(final String hash, final int strength) {
        return hash.endsWith(String.join("", Collections.nCopies(strength, "0")));
    }
}
