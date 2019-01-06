package io.triada.models.key;

import io.triada.models.cli.CommandLineInterface;
import io.triada.models.cli.ShellScript;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RsaKey implements Key {

    /**
     * Lazy RSA value , calculated only once
     */
    private final ConcurrentMap<String, String> rsa = new ConcurrentHashMap<>(2, 1, 2);

    /**
     * File content
     */
    private final String content;

    /**
     * Is public key
     */
    private final boolean isPublicKey;

    /**
     * Command line Interface
     */
    private final CommandLineInterface<String> cli;

    public RsaKey(final File file, final CommandLineInterface<String> cli) throws IOException {
        validate(file);
        this.content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        this.cli = cli;
        this.isPublicKey = this.content.contains("PUBLIC KEY-----");
    }

    public RsaKey(final File file) throws IOException {
        this(
                file,
                new ShellScript()
        );
    }

    public RsaKey(final String content, final CommandLineInterface<String> cli) {
        if (content.startsWith("-----")) {
            this.content = content;
        } else {
            this.content = String.join(
                    "\n",
                    "-----BEGIN PUBLIC KEY-----",
                    content.replaceAll("(?<=\\G.{64})", "\n"),
                    "-----END PUBLIC KEY-----"
            );
        }
        this.cli = cli;
        this.isPublicKey = this.content.contains("PRIVATE KEY-----");
    }

    @Override
    public String sign(final String text) throws Exception {
        if (!this.isPublicKey) {
            final PrivateKey pk = new PrivateKeyFromFile(this.rsa()).call();
            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(pk);
            signature.update(text.getBytes(StandardCharsets.UTF_8));
            final byte[] sign = signature.sign();
            return Base64.getEncoder().encodeToString(sign);
        }
        throw new IllegalStateException("Can't sign using public key");
    }

    @Override
    public boolean verify(final String signature, final String text) throws Exception {
        return false;
    }

    @Override
    public String asPublic() {
        return this.rsa().replace("\n", "").replaceAll("-{5}[ A-Z]+-{5}", "");
    }

    /**
     * @return RSA key
     */
    private String rsa() {
        return this.rsa.computeIfAbsent(this.content, key -> {
            final String trimed = this.content.trim();
            if (this.isPublicKey) {
                try {
                    return this.encodePublicKey(trimed);
                } catch (final IOException exc) {
                    throw new UncheckedIOException(exc);
                }
            } else {
                return trimed;
            }
        });

    }

    /**
     * Validate given file
     *
     * @param file to validate
     */
    private static void validate(final File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            final String fileName = file == null ? "null" : file.getName();
            throw new IllegalArgumentException(
                    String.format(
                            "Can't find RSA key at %s",
                            fileName
                    )
            );
        }
    }

    /**
     * @param publicKey ssh-rsa public key
     * @return encoded to pkcs8 text
     * @throws IOException if failed
     */
    private String encodePublicKey(final String publicKey) throws IOException {
        final File tempFile = File.createTempFile("/tmp/", ".tmp");
        FileUtils.write(tempFile, publicKey, StandardCharsets.UTF_8);//write ssh-rsa
        final String pkcs1 = this.PKCS1RSAkey(tempFile);
        FileUtils.write(tempFile, "", StandardCharsets.UTF_8);//clear content
        FileUtils.write(tempFile, pkcs1, StandardCharsets.UTF_8);//write pkcs1
        final String pkcs8 = this.pkcs1To8(tempFile);
        tempFile.delete();
        return pkcs8;
    }

    /**
     * @param tempFile file with ssh-rsa content
     * @return PKCS#1 public key format
     * @throws IOException if failed
     */
    private String PKCS1RSAkey(final File tempFile) throws IOException {
        return this.cli.executeCommand(String.format("ssh-keygen -f %s -e -m pem", tempFile.getAbsolutePath()));
    }

    /**
     * @param tempFile with PKCS#1 content
     * @return PKCS#8 format
     * @throws IOException if failed
     */
    private String pkcs1To8(final File tempFile) throws IOException {
        return this.cli.executeCommand(String.format("openssl rsa -RSAPublicKey_in -in %s -pubout", tempFile.getAbsolutePath()));
    }
}
