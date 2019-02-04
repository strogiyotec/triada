package io.triada.node.farm;

import io.triada.models.score.Score;
import io.triada.text.Jsonable;
import io.triada.text.Text;

import java.util.List;

public interface Farm extends Text, Jsonable {
    void start() throws Exception;

    void cleanUp() throws Exception;

    void save();

    List<Score> best() throws Exception;
}
