package io.triada.models.wallet;

import io.triada.text.Text;

import java.util.Date;

public interface CsvCopy extends Text{
    String name();

    String host();

    int port();

    int score();

    Date time();

    boolean master();
}
