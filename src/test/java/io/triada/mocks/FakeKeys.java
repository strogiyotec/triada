package io.triada.mocks;

import io.triada.models.key.RsaKey;
import org.springframework.util.ResourceUtils;

import java.io.File;

public final class FakeKeys {

    public File publicKeyFile() throws Exception {
        return ResourceUtils.getFile(
                this.getClass().getResource("/keys/id_rsa.pub")
        );
    }

    public RsaKey privateKey() throws Exception {
        return new RsaKey(privateKeyFile());
    }

    public RsaKey publicKey() throws Exception {
        return new RsaKey(publicKeyFile());
    }

    public File privateKeyFile() throws Exception {
        return ResourceUtils.getFile(
                this.getClass().getResource("/keys/pkcs8")
        );
    }
}
