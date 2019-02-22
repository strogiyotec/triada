package io.triada.node.farm;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.models.score.Score;
import io.triada.text.Jsonable;
import io.triada.text.Text;
import org.jooq.lambda.fi.lang.CheckedRunnable;

import java.util.Collections;
import java.util.List;

public interface Farm extends Text, Jsonable {
    void start(HostAndPort hostAndPort, CheckedRunnable runnable) throws Throwable;

    List<Score> best() throws Exception;

    final class Empty implements Farm {

        @Override
        public void start(final HostAndPort hostAndPort, final CheckedRunnable runnable) throws Throwable {
            runnable.run();
        }

        @Override
        public List<Score> best() throws Exception {
            return Collections.emptyList();
        }

        @Override
        public JsonObject asJson() {
            return new JsonObject();
        }

        @Override
        public String asText() {
            return "empty";
        }
    }
}
