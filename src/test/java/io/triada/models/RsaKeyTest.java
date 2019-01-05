package io.triada.models;

import io.triada.models.key.RsaKey;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

public final class RsaKeyTest extends Assert {

    @Test
    public void testToPublic() throws Exception {
        final RsaKey rsaKey =
                new RsaKey(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/keys/id_rsa.pub")
                        )
                );
        assertTrue(rsaKey.asPublic().startsWith("MIICI"));
    }
}
