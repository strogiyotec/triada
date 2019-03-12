package io.triada.mocks;

import org.springframework.util.ResourceUtils;

import java.io.File;

public final class FakeKeys {

    public File publicKey() throws Exception {
        return ResourceUtils.getFile(
                this.getClass().getResource("/keys/id_rsa.pub")
        );
    }

    public File privateKey() throws Exception {
        return ResourceUtils.getFile(
                this.getClass().getResource("/keys/pkcs8")
        );
    }
}
