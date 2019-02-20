package io.triada.mocks;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Create temp file
 */
public final class FakeFile implements Callable<File> {

    private final File origin;

    public FakeFile(final String ext) throws IOException {
        final File tempFile = File.createTempFile("/tmp/", ext);
        tempFile.deleteOnExit();
        this.origin = tempFile;
    }

    @Override
    public File call() throws Exception {
        return this.origin;
    }
}
