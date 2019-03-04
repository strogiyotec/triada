package io.triada.models.tax;

import io.triada.models.key.RsaKey;
import io.triada.models.score.Score;
import io.triada.models.transaction.SignedTransaction;
import io.triada.text.Text;

import java.util.Optional;

public interface Tax extends Text {

    boolean exists(String details);

    Tax pay(RsaKey rsaKey, Score score) throws Exception;

    long paid();

    long debt();

    Optional<SignedTransaction> last();

    String details(Score score);

}