package io.triada.text;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public final class HexTextTest extends Assert {

    @Test
    public void testHexEquals() {
        assertThat(
                new HexText(1184351502415558391L),
                is("106fa96a9eadf6f7")
        );
    }

    @Test
    public void testHexNotEquals() {
        assertThat(
                new HexText(1184351502415558391L),
                not("106fa96a9eadf6fdf7")
        );
    }
}
