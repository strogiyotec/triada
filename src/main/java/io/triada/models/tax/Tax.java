package io.triada.models.tax;

import io.triada.text.Text;

public interface Tax extends Text {

    void pay() throws Exception;

    int paid() throws Exception;

    long debt() throws Exception;


}
