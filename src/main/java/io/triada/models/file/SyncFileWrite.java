package io.triada.models.file;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

@UtilityClass
public final class SyncFileWrite {

    /**
     * Requires exclusive lock to file and append body
     *
     * @param body Text to append
     * @param file File to append
     * @throws Exception if failed
     */
    public void write(final String body, final File file) throws Exception {
        try (final RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            try (final FileChannel channel = accessFile.getChannel()) {
                try (final FileLock ignored = channel.lock()) {
                    try (final FileWriter writer = new FileWriter(file, true)) {
                        writer.append(body).append(System.lineSeparator());
                    }
                }
            }
        }
    }
}
