package io.triada.models.patch;

import com.google.common.hash.Hashing;
import io.triada.dates.DateConverters;
import io.triada.functions.CheckedToBooleanFunction;
import io.triada.models.Exceptionally;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.sign.TxnSignature;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.wallet.EmptyWallet;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class in not thread safe
 */
public final class TxnsPatch implements Patch {

    private final Wallets wallets;

    private final List<SignedTransaction> txns = new ArrayList<>(16);

    private String id;

    private String key;

    private String network;

    public TxnsPatch(
            final Wallets wallets,
            final String id,
            final String key,
            final String network
    ) {
        this.wallets = wallets;
        this.id = id;
        this.key = key;
        this.network = network;
    }

    public TxnsPatch(final Wallets wallets) {
        this.wallets = wallets;
    }

    /**
     * Add legacy transactions first, since they are negative and can't
     * be deleted ever. This method is called by merge command in order to add
     * legacy negative transactions to the patch before everything else. They
     * are not supposed to be disputed, ever.
     *
     * @param wallet Wallet with txns
     * @param hours  Hours
     */
    @Override
    public void legacy(final Wallet wallet, final int hours) {
        for (final SignedTransaction txn : wallet.transactions()) {
            final ParsedTxnData data = new ParsedTxnData(txn);
            if (data.amount().lessOrEq(0L) && (data.date().compareTo(DateConverters.nowMinusHours(hours)) <= 0)) {
                this.txns.add(txn);
            }
        }
    }

    /**
     * Joins a new wallet on top of existing patch. An attempt is made to
     * copy as many transactions from the newcoming wallet to the existing
     * set of transactions, avoiding mistakes and duplicates.
     * <p>
     * A block has to be given. It will be called, if a paying wallet is absent.
     * The block will have to return either TRUE or FALSE. TRUE will mean that
     * the paying wallet has to be present and we just tried to pull it. If it's
     * not present, it's a failure, don't accept the transaction. FALSE will mean
     * that the transaction should be accepted, even if the paying wallet is
     * absent.
     *
     * @param wallet Wallet
     * @param ledger Ledger
     * @param yield  Callback
     */
    @Override
    public void join(final Wallet wallet, final File ledger, final CheckedToBooleanFunction<SignedTransaction> yield) throws Exception {
        if (this.id == null) {
            this.id = wallet.head().id();
            this.key = wallet.head().key();
            this.network = wallet.head().network();
        }
        if (!isWalletValid(wallet)) {
            return;
        }
        final TxnSignature signature = new TxnSignature();
        final List<ParsedTxnData> txnsData =
                this.txns
                        .stream()
                        .map(ParsedTxnData::new)
                        .collect(Collectors.toList());
        for (final SignedTransaction txn : wallet.transactions()) {
            if (this.txns.contains(txn)) {
                continue;
            }
            final ParsedTxnData data = new ParsedTxnData(txn);
            if (data.amount().lessOrEq(0L)) {
                final boolean duplicate =
                        txnsData.stream()
                                .anyMatch(
                                        txnData -> txnData.id() == data.id() &&
                                                txnData.amount().lessOrEq(0L)
                                );
                if (duplicate) {
                    System.out.println("Attempt to override existing txn");
                    continue;
                }
                final long balance =
                        txnsData.stream()
                                .mapToLong(txnData -> data.amount().value())
                                .sum();
                if (balance < data.amount().value() * -1 && !wallet.head().id().equals(LongId.ROOT.asText())) {
                    System.out.printf("Txn %s try to make balance as negative\n", data.id());
                    continue;
                }
                if (!signature.valid(new RsaKey(this.key), new LongId(wallet.head().id()), txn)) {
                    System.out.printf("Invalid RSA signature at txn %s", data.id());
                    continue;
                }
            } else {
                final boolean duplicate = txnsData.stream()
                        .anyMatch(
                                txnData -> data.id() == txnData.id() &&
                                        data.bnf().asText().equals(txnData.bnf().asText()) &&
                                        txnData.amount().bigger(0L)
                        );
                if (duplicate) {
                    System.out.println("Overwriting existing txn");
                    continue;
                }
                if (!wallet.head().key().contains(data.prefix())) {
                    System.out.printf(
                            "Payment prefix %s doesn't match with key of wallet %s\n",
                            data.prefix(),
                            wallet.head().id()
                    );
                    continue;
                }
                if (Exceptionally.hasException(() -> this.wallets.acq(data.bnf().asText()))) {
                    if (yield.apply(txn) && Exceptionally.hasException(() -> this.wallets.acq(data.bnf().asText()))) {
                        System.out.printf(
                                "Paying wallet is absent even after pull %d\n",
                                data.id()
                        );
                        continue;
                    }
                }
                final Wallet acq = this.wallets.acq(data.bnf().asText());
                final boolean hasTxn =
                        acq.transactions()
                                .stream()
                                .map(ParsedTxnData::new)
                                .anyMatch(
                                        txnData -> txnData.id() == data.id() &&
                                                txnData.bnf().asText().equals(data.bnf().asText())
                                );
                if (!hasTxn) {
                    System.out.println("Doesn't have this txn");
                    continue;
                }

            }
            this.txns.add(txn);
            if (data.amount().lessOrEq(0L)) {
                final String line = String.join(
                        ";",
                        String.valueOf(new Date().getTime()),
                        String.valueOf(data.id()),
                        String.valueOf(data.date().getTime()),
                        wallet.head().id(),
                        data.bnf().asText(),
                        data.amount().mpy(-1L).asText(2),
                        data.prefix(),
                        data.details(),
                        System.lineSeparator()
                );
                Files.write(
                        ledger.toPath(),
                        line.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND
                );
            }
        }
    }

    @Override
    public boolean empty() {
        return this.id == null;
    }

    @Override
    public boolean save(final File file, final boolean overwrite) throws IOException {
        if (this.empty()) {
            throw new IllegalStateException("You have to join at least one wallet ");
        }
        final String before = Hashing.sha256().hashBytes(Files.readAllBytes(file.toPath())).toString();
        new EmptyWallet(
                this.id,
                this.key,
                this.network,
                overwrite,
                file.getAbsolutePath()

        );
        for (final SignedTransaction transaction : this.txns) {
            Files.write(
                    file.toPath(),
                    (transaction.asText() + "\n").getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND
            );
        }

        final String after = Hashing.sha256().hashBytes(Files.readAllBytes(file.toPath())).toString();
        return !before.equals(after);
    }

    @Override
    public String asText() {
        if (this.txns.isEmpty()) {
            return "nothing";
        } else {
            return this.txns.size() + " txns";
        }
    }

    private boolean isWalletValid(final Wallet wallet) {
        if (!Objects.equals(this.network, wallet.head().network())) {
            System.out.printf(
                    "The wallet is from different network %s , our is %s\n",
                    wallet.head().network(),
                    this.network
            );
            return false;
        }
        if (!Objects.equals(this.key, wallet.head().key())) {
            System.out.println("Public key mismatch");
            return false;
        }
        if (!Objects.equals(this.id, wallet.head().id())) {
            System.out.printf(
                    "Wallet id mismatch , ours is %s theirs is %s\n",
                    this.id,
                    wallet.head().id()
            );
            return false;
        }
        return true;
    }
}
