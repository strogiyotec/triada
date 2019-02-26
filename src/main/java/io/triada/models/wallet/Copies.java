package io.triada.models.wallet;

import com.google.common.net.HostAndPort;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface Copies {

    String add(String content, HostAndPort hostAndPort, int score, Date time, boolean master) throws IOException;

    default String add(String content, HostAndPort hostAndPort, int score) throws Exception {
        return this.add(content, hostAndPort, score, new Date(), false);
    }

    void remove(HostAndPort hostAndPort) throws IOException;

    int clean() throws IOException;

    List<AllCopy> all() throws IOException;

    List<CsvCopy> load() throws IOException;

}
