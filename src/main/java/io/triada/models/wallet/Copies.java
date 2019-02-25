package io.triada.models.wallet;

import com.google.common.net.HostAndPort;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface Copies {

    void add(String content, HostAndPort hostAndPort, int score, Date time, boolean master);

    void remove(HostAndPort hostAndPort) throws IOException;

    void clean();

    List<AllCopy> all();

    List<CsvCopy> load() throws IOException;

}
