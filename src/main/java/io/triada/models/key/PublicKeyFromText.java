package io.triada.models.key;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PublicKeyFromText implements Callable<PublicKey> {

    private final byte[] content;

    public PublicKeyFromText(final File file) throws Exception {
        this.content = FileUtils.readFileToString(file, UTF_8).getBytes(UTF_8);
    }

    public PublicKeyFromText(final String content) throws Exception {
        this.content = content.getBytes(UTF_8);
    }

    @Override
    public PublicKey call() throws Exception {
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(this.content);
        final KeyFactory rsa = KeyFactory.getInstance("RSA");

        return rsa.generatePublic(spec);
    }
}
