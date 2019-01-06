package io.triada.models.key;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PrivateKeyFromFile implements Callable<PrivateKey> {

    private final byte[] content;

    public PrivateKeyFromFile(final File file) throws Exception {
        this(FileUtils.readFileToString(file, UTF_8));
    }

    public PrivateKeyFromFile(final String content) throws Exception {
        this.content = content.replaceAll("-----\\w+ PRIVATE KEY-----", "").replaceAll("\\s", "").getBytes(UTF_8);
    }

    @Override
    public PrivateKey call() throws Exception {
        byte[] decoded = Base64.getDecoder().decode(this.content);
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
