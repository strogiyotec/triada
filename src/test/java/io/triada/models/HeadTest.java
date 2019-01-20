package io.triada.models;

import io.triada.models.head.HeadOfWallet;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public final class HeadTest extends Assert {

    @Test
    public void testParseHead() throws Exception {
        final String network = "triada";
        final String protocol = "322";
        final String id  = "140234234";
        final String key = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1w5FGfMDNWYpgjHsIOGQyXUpfjjoxszPIkmqkiTQr6gvqN5PgYfPwOOFesQV4rrnbttofNkqpYZTB+XIfjbf41IGodvT11MeUIbQJDqiXcGpaxvkwf1zmMn1KceUqQjTDuvk5LLFFvFLBkKjJEfaCn2fDz2W4CuKllp45eW/oBN8LjMJVfRjlq1n4n54aAPhW/wIg+PfN6kLA35GADSBcQmPanfCxaFbunMmLHhGcYBoDxHrzW09z2/+e0vqPrQusfNtllkDw2GxR0gTjFDcl+MRgsyWv3CQ1mJ5wXfPD6PJ5ZtjPOJRq9hk7iZFueR/CGa1UF94vz/WLsQ4ohzGpeA4rywvWLe1sZQkV1gwG675IOpRAjCcmhpb/MohJQMOVnAkAyyxMfSTzVX3oNymCAvrtqGkQ621P/jNFwBsWY9KUN/fPp3uAXoK/iwoBRYCkGrz+d3cT7qf57wvglamQFwOCpODGCeLGo1oeWMEz/MsPgqTezebYoL8WpaYHkScrQE+UGP2mtpvT4fx/L6jsQfVKddAKrGSI8vstCkmunQSbcTbatZOEa+SIw2g5/H2cdnO4yIoDa57JaEVMlqfOOGsXn3gpxB0CfgmDKI9eudt7yXHd+nsEc+elN6n6g1kcp+ugkmT8ZindQYcggWMxo1dYEjnQje4/C0+jsBsB10CAwEAAQ==";
        final HeadOfWallet head = new HeadOfWallet(
                String.join(
                        System.lineSeparator(),
                        network,
                        protocol,
                        id,
                        key
                )
        );
        assertThat(head.network(),is(network));
        assertThat(head.protocol(),is(protocol));
        assertThat(head.id(),is(id));
        assertThat(head.key(),is(key));
    }
}
