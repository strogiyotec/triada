package io.triada.models.key;

public interface Key {
    String sign();

    boolean verify(String signature, String text);

    String asPublic();
}
