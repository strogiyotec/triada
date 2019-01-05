package io.triada.models.key;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class RsaKey implements Key {

    /**
     * File content
     */
    private final String content;

    public RsaKey(final File file) throws IOException {
        validate(file);
        this.content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    public RsaKey(final String content) {
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
    }

    @Override
    public String sign() {
        return null;
    }

    @Override
    public boolean verify(final String signature, final String text) {
        return false;
    }

    @Override
    public String asPublic() {
        return this.content.replace("\n", "").replaceAll("-{5}[ A-Z]+-{5}", "");
    }

    private String rsa(){
        final String text  = this.content.trim();
        return text;
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
}
