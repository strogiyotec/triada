package io.triada.models;

import io.triada.mocks.FakeHeadFile;
import io.triada.models.id.LongId;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;

public final class FakeHeadTest extends Assert {

    @Test
    public void testFakeHeadCreation() throws Exception {
        final File file = new FakeHeadFile().fakeHome(new LongId());
        assertThat(
                FileUtils.readFileToString(
                        file,
                        "UTF-8"
                ).split(System.lineSeparator()).length,
                is(5)
        );
    }
}
