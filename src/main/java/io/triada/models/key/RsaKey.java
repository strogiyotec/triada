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
     * Rsa algorithm
     */
    private static final String RSA_ALG = "SHA256withRSA";

    /**
     * Lazy RSA value , calculated only once
     * // TODO: 2/8/19 rewrite using Google guava cache
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

    /**
     * Ctor
     */
    public RsaKey(final File file, final CommandLineInterface<String> cli) throws IOException {
        validate(file);
        this.content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        this.cli = cli;
        this.isPublicKey = this.content.contains("ssh-rsa");
    }

    /**
     * Ctor
     */
    public RsaKey(final File file) throws IOException {
        this(
                file,
                new ShellScript()
        );
    }

    /**
     * Ctor
     */
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
        this.isPublicKey = this.content.contains("ssh-rsa");
    }

    /**
     * Ctor
     */
    public RsaKey(final String content) {
        this(content, new ShellScript());
    }

    /**
     * @param text to sign
     * @return signed text
     * @throws Exception if key is not a private key
     */
    @Override
    public String sign(final String text) throws Exception {
        if (!this.isPublicKey) {
            final PrivateKey pk = new PrivateKeyFromFile(this.rsa()).call();
            final Signature signature = Signature.getInstance(RSA_ALG);
            signature.initSign(pk);
            signature.update(text.getBytes(StandardCharsets.UTF_8));
            final byte[] sign = signature.sign();
            return Base64.getEncoder().encodeToString(sign);
        }
        throw new IllegalStateException("Can't sign using public key");
    }

    /**
     * @param signature The sign
     * @param text      Text to be verified
     * @return <tt>TRUE</tt> if text verified by signature
     * @throws Exception if failed
     */
    @Override
    public boolean verify(final String signature, final String text) throws Exception {
        final Signature publicSignature = Signature.getInstance(RSA_ALG);
        publicSignature.initVerify(new PublicKeyFromText(this.rsa()).call());
        publicSignature.update(text.getBytes(StandardCharsets.UTF_8));

        final byte[] decode = Base64.getDecoder().decode(signature);
        return publicSignature.verify(decode);
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
            try {
                if (this.isPublicKey) {
                    return this.encodePublicKey(trimed);
                } else {
                    return trimed;
                }
            } catch (final IOException exc) {
                throw new UncheckedIOException(exc);
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

    @Override
    public String toString() {
        return this.rsa();
    }
}
