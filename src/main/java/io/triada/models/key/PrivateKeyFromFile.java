package io.triada.models.key;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PrivateKeyFromFile implements Callable<PrivateKey> {

    private final byte[] content;

    public PrivateKeyFromFile(final File file) throws Exception {
        this.content = FileUtils.readFileToString(file, UTF_8).getBytes(UTF_8);
    }

    public PrivateKeyFromFile(final String content) throws Exception {
        this.content = content.getBytes(UTF_8);
    }

    @Override
    public PrivateKey call() throws Exception {
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(this.content);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
