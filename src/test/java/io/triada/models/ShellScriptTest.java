package io.triada.models;

import io.triada.models.cli.ShellScript;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public final class ShellScriptTest extends Assert {

    @Test
    public void testEchoCommand() throws Exception {
        assertThat(
                new ShellScript().executeCommand("echo -e \"Hello world\\c\""),
                is("\"Hello world")
        );
    }
}
