package io.triada.models;

import io.triada.models.transactions.SignedTxnsFromFile;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import static org.hamcrest.CoreMatchers.is;

public final class SignedTxnFromFileTest extends Assert {

    @Test
    public void readTxnsFromFileTest() throws Exception {
        final SignedTxnsFromFile txns =
                new SignedTxnsFromFile(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/wallet/448b451bc62e8e16.trd")
                        )
                );
        assertThat(txns.txns().size(),is(1000));
    }
}
