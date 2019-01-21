package io.triada.models;

import io.triada.models.amount.TxnAmount;
import io.triada.models.id.WalletId;
import io.triada.models.key.RsaKey;
import io.triada.models.sign.TriadaSignature;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.transaction.ValidatedTxn;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;

public final class SignatureTest extends Assert {

    @Test
    public void testSignAndValdiate() throws Exception {
        final TriadaSignature sign = new TriadaSignature();
        final RsaKey priv = new RsaKey(
                ResourceUtils.getFile(
                        this.getClass().getResource("/keys/pkcs8")
                )
        );
        final RsaKey pub = new RsaKey(
                ResourceUtils.getFile(
                        this.getClass().getResource("/keys/id_rsa.pub")
                )
        );
        final WalletId walletId = new WalletId();
        final SignedTriadaTxn signedTxn = new SignedTriadaTxn(
                new ValidatedTxn(
                        "fffffc6f00000000",
                        new Date(),
                        new TxnAmount(1000L),
                        "NOPREFIX",
                        new WalletId(),
                        "Hello . This is sign test"
                )
                , priv, walletId);
        assertThat(signedTxn.signature().length(), is(684));
        assertThat(sign.valid(pub, walletId, signedTxn), is(true));


    }
}
