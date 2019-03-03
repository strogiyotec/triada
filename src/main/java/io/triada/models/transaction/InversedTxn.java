package io.triada.models.transaction;

import io.triada.models.id.LongId;

public final class InversedTxn extends TxnEnvelope {

    public InversedTxn(final ParsedTxnData origin, final LongId bnf) {
        super(
                new TriadaTxn(
                        origin.id(),
                        origin.date(),
                        origin.amount().mpy(-1L),
                        origin.prefix(),
                        bnf,
                        origin.details()
                ));
    }
}
