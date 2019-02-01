package io.triada.models.tax;

import io.triada.models.key.RsaKey;
import io.triada.models.score.Score;
import io.triada.models.transaction.SignedTransaction;
import io.triada.text.Text;

import java.util.Optional;

public interface Tax extends Text {

    Tax pay(RsaKey rsaKey, Score score) throws Exception;

    long paid() throws Exception;

    long debt() throws Exception;

    Optional<SignedTransaction> last();

    String details(Score score);
}
