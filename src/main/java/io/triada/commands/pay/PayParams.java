package io.triada.commands.pay;

import com.google.common.base.Predicates;
import io.triada.models.amount.TxnAmount;
import io.triada.models.tax.Tax;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
final class PayParams {
    private final List<String> params;

    public String payerWalletId() {
        return this.params.stream()
                .filter(p -> p.contains("payer"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .orElseThrow(() -> new IllegalStateException("Payer wallet ID is required as the first argument"));
    }

    public String invoice() {
        return this.params.stream()
                .filter(p -> p.contains("recipient"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .orElseThrow(() -> new IllegalStateException("Payer wallet ID is required as the first argument"));
    }

    public TxnAmount amount() {
        return new TxnAmount(
                new BigDecimal(
                        this.params.stream()
                                .filter(p -> p.contains("amount"))
                                .map(p -> p.substring(p.indexOf("=") + 1))
                                .findFirst()
                                .filter(Predicates.not(String::isEmpty))
                                .map(p -> p.replaceAll("trd", ""))
                                .orElseThrow(() -> new IllegalStateException("Amount is required")
                                )
                )
        );
    }

    public Tax taxes(final Wallets wallets) throws Exception {
        final Wallet wallet = wallets.acq(this.payerWalletId());
        // TODO: 3/3/19 Need taxes command
        return null;
    }

    public String details() {
        return this.params.stream()
                .filter(p -> p.contains("details"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .orElse("-");
    }

}
