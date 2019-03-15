package io.triada.commands;

import io.triada.commands.calculate.CalculateCommand;
import io.triada.models.score.Score;
import org.junit.Assert;
import org.junit.Test;

public final class TestCalculateCommand extends Assert {

    @Test
    public void testCalculateScore() throws Exception{
        final Score score = new CalculateCommand().run(
                new String[]{
                        "-calculate",
                        "strength=2",
                        "max=8",
                        "invoice=" + "NOSUFFIX@ffffffffffffffff"
                }
        );
        assertTrue(score.valid());
        assertEquals(8,score.value());
    }
}
