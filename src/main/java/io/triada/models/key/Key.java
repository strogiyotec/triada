package io.triada.models.key;

public interface Key {

    /**
     * @param text to sign
     * @return signed text
     * @throws Exception if failed
     */
    String sign(String text) throws Exception;

    /**
     * @param signature The sign
     * @param text      Text to be verified
     * @return true if text was verified
     * @throws Exception if failed
     */
    boolean verify(String signature, String text) throws Exception;

    /**
     * @return public representation of text
     */
    String asPublic();
}
