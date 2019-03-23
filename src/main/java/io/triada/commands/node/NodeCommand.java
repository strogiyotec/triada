package io.triada.commands.node;

import io.triada.commands.Command;
import io.triada.node.farm.Farm;

public final class NodeCommand implements Command{
    @Override
    public void run(final String[] argc) throws Exception {
        new Farm()
    }
}
