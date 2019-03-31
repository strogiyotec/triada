package io.triada.models.wallet;

import com.google.common.net.HostAndPort;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface Copies {

    /**
     * @return Root of copies
     */
    File root();

    String add(String content, HostAndPort hostAndPort, int score, Date time, boolean master) throws IOException;

    void remove(HostAndPort hostAndPort) throws IOException;

    int clean(int days) throws IOException;

    List<WalletCopy> all() throws IOException;

    List<CsvCopy> load() throws IOException;

    default String add(String content, HostAndPort hostAndPort, int score) throws Exception {
        return this.add(content, hostAndPort, score, new Date(), false);
    }

    default String add(String content, HostAndPort hostAndPort, int score,final Date date) throws Exception {
        return this.add(content, hostAndPort, score, date, false);
    }

    default String add(String content, String host, int port, int score) throws Exception {
        return this.add(content, HostAndPort.fromParts(host, port), score, new Date(), false);
    }

    default String add(String content, HostAndPort hostAndPort, int score, boolean master) throws Exception {
        return this.add(content, hostAndPort, score, new Date(), master);
    }

    default int clean() throws Exception {
        return this.clean(1);
    }

    default void remove(String host,int port) throws Exception{
        this.remove(HostAndPort.fromParts(host,port));
    }
}
