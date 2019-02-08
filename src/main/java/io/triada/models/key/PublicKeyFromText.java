package io.triada.models.key;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Create public key from file
 */
public final class PublicKeyFromText implements Callable<PublicKey> {

    /**
     * Public key
     */
    private final byte[] content;

    public PublicKeyFromText(final File file) throws Exception {
        this(FileUtils.readFileToString(file, UTF_8));
    }

    public PublicKeyFromText(final String content) throws Exception {
        this.content = content.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s", "").getBytes(UTF_8);
    }

    @Override
    public PublicKey call() throws Exception {
        byte[] decoded = Base64.getDecoder().decode(this.content);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
