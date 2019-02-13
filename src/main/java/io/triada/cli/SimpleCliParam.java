package io.triada.cli;

import java.util.Collections;
import java.util.List;

public final class SimpleCliParam implements CliParam {

    private final List<String> names;

    private final List<String> argc;

    private final String description;

    public SimpleCliParam(
            final List<String> names,
            final List<String> argc,
            final String description
    ) {
        this.names = names;
        this.argc = argc;
        this.description = description;
    }

    public SimpleCliParam(
            final List<String> names,
            final String description
    ) {
        this.names = names;
        this.argc = Collections.emptyList();
        this.description = description;
    }

    public SimpleCliParam(
            final List<String> names,
            final List<String> argc
    ) {
        this.names = names;
        this.argc = argc;
        this.description = "No description";
    }

    public SimpleCliParam(
            final String name,
            final List<String> argc
    ) {
        this.names = Collections.singletonList(name);
        this.argc = argc;
        this.description = "No description";
    }

    public SimpleCliParam(
            final String name,
            final List<String> argc,
            final String description
    ) {
        this.names = Collections.singletonList(name);
        this.argc = argc;
        this.description = description;
    }

    @Override
    public List<String> names() {
        return this.names;
    }

    @Override
    public List<String> argc() {
        return this.argc;
    }

    @Override
    public String description() {
        return this.description;
    }
}
