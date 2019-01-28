package io.triada.models.hash;

import java.math.BigInteger;
import java.util.Collections;

/**
 * Calculate hash of transaction
 */
public final class BigIntegerHash extends HashEnvelope {

    public BigIntegerHash(final String prefix, final int strength) {
        super(
                () -> {
                    BigInteger nonce = BigInteger.ONE;
                    final String zeroes = String.join("", Collections.nCopies(strength, "0"));
                    while (true) {
                        nonce = nonce.add(BigInteger.ONE);
                        final String hash = Hash.sha256(prefix, nonce.toString());
                        if (checkHash(hash, zeroes)) {
                            return new ConstTxnHash(hash, nonce.toString());
                        }
                    }
                }
        );
    }

    /**
     * @param hash     Hash
     * @param strength amount of zeros
     * @return True if hash end with with amount of zeros
     */
    private static boolean checkHash(final String hash, final String strength) {
        return hash.endsWith(strength);
    }
}
