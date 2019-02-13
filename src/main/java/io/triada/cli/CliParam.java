package io.triada.cli;

import java.util.List;

public interface CliParam {

    List<String> names();

    List<String> argc();

    String description();


}
