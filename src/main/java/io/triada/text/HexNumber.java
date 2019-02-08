package io.triada.text;

import lombok.AllArgsConstructor;

/**
 * Convert given number to fixed length hex value
 */
@AllArgsConstructor
public final class HexNumber implements Text {

    /**
     * Length of hex number
     */
    private final int length;

    /**
     * Number to convert to hex
     */
    private final long number;

    @Override
    public String asText() {
        return String.format(
                "%0" + this.length + "x",
                this.number
        ).replaceAll("^\\.{2}", "ff");
    }
}
