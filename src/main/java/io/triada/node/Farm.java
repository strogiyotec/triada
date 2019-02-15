package io.triada.node;

import com.google.gson.JsonObject;
import io.triada.models.score.Score;
import io.triada.text.Jsonable;
import io.triada.text.Text;

import java.util.Collections;
import java.util.List;

public interface Farm extends Text, Jsonable {
    void start() throws Exception;

    void cleanUp() throws Exception;

    void save();

    List<Score> best() throws Exception;

    final class Empty implements Farm {

        @Override
        public void start() throws Exception {

        }

        @Override
        public void cleanUp() throws Exception {

        }

        @Override
        public void save() {

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
