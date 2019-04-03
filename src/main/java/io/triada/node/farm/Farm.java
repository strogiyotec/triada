package io.triada.node.farm;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.functions.CheckedRunnable;
import io.triada.models.score.Score;
import io.triada.text.Jsonable;
import io.triada.text.Text;

import java.util.Collections;
import java.util.List;

public interface Farm extends Text, Jsonable {

    Empty EMPTY = new Empty();

    void start(HostAndPort hostAndPort, CheckedRunnable runnable) throws Exception;

    List<Score> best() throws Exception;

    final class Empty implements Farm {

        @Override
        public void start(final HostAndPort hostAndPort,final CheckedRunnable runnable) throws Exception {
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
