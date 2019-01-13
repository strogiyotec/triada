package io.triada.models;

import io.triada.models.key.RsaKey;
import io.triada.models.prefix.PaymentPrefix;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import static org.hamcrest.CoreMatchers.is;

public final class PrefixTest extends Assert {
    @Test
    public void testCreatePrefix() throws Exception {
        assertThat(
                new PaymentPrefix(
                        new RsaKey(
                                ResourceUtils.getFile(
                                        this.getClass().getResource("/keys/id_rsa.pub")
                                )
                        )
                ).create(10).length(),
                is(10)
        );
    }
}
