package io.triada.models.head;

/**
 * First four lines in wallet file
 */
public interface Head {

    /**
     *
     * @return Network name
     */
    String network();

    /**
     *
     * @return Protocol name
     */
    String protocol();

    /**
     *
     * @return Wallet id
     */
    String id();

    /**
     *
     * @return Public key of wallet as text
     */
    String key();

}
