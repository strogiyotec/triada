package io.triada.models;

import io.triada.models.key.PrivateKeyFromFile;
import io.triada.models.key.PublicKeyFromText;
import io.triada.models.key.RsaKey;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import static org.hamcrest.CoreMatchers.is;

public final class RsaKeyTest extends Assert {

    @Test
    public void testPublicKey() throws Exception {
        final RsaKey rsaKey =
                new RsaKey(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/keys/id_rsa.pub")
                        )
                );
        assertTrue(rsaKey.asPublic().startsWith("MIICI"));
    }

    @Test
    public void testPrivateKey() throws Exception {
        final RsaKey rsaKey =
                new RsaKey(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/keys/id_rsa")
                        )
                );
        assertTrue(rsaKey.asPublic().startsWith("MIIJJ"));
    }

    @Test
    public void testSignAndVerify() throws Exception {
        final RsaKey privateKey =
                new RsaKey(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/keys/pkcs8")
                        )
                );
        final RsaKey publicKey =
                new RsaKey(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/keys/id_rsa.pub")
                        )
                );
        final String text = "Hello world";
        final String signed = privateKey.sign(text);
        assertTrue(publicKey.verify(signed, text));
    }

    @Test
    public void testPrivateKeyToPkcs8() throws Exception {
        final PrivateKeyFromFile privateKeyFromFile = new PrivateKeyFromFile(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        assertThat(privateKeyFromFile.call().getAlgorithm(), is("RSA"));
    }

    @Test
    public void testPublicKeyToPkcs8() throws Exception {
        final PublicKeyFromText privateKeyFromFile = new PublicKeyFromText(new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/id_rsa.pub"))).toString());
        assertThat(privateKeyFromFile.call().getAlgorithm(), is("RSA"));
    }
}
