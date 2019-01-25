package io.triada.text;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public final class HexNumberTest extends Assert {

    @Test
    public void testHexFromOne() {
        assertThat(new HexNumber(4, 1).asText(), is("0001"));
    }

    @Test
    public void testHex16() {
        assertThat(new HexNumber(16, 1).asText(), is("0000000000000001"));
    }
}
