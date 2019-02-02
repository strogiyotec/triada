package io.triada.models.score;

import com.google.common.net.HostAndPort;
import io.triada.text.Text;

import java.util.Date;
import java.util.List;

public interface Score extends Text{

    /**
     * @return New suffix
     */
    Score next();

    /**
     * @return List of suffixes
     */
    List<String> suffixes();

    /**
     * @return Created at date of score
     */
    Date createdAt();

    /**
     * @return Address of score
     */
    HostAndPort address();

    /**
     * @return Invoice of the score
     */
    String invoice();

    /**
     * @return Amount of zeros to create suffix
     */
    int strength();

    /**
     * @return Time score was created
     */
    Date time();

    /**
     * @return Hash of score
     */
    String hash();

    /**
     * @return Length of suffixes
     */
    int value();

    /**
     * @param hours Hours value
     * @return True if the age of the score is over given hours
     */
    boolean expired(int hours);
}
