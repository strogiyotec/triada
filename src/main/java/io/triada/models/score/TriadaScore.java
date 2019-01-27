package io.triada.models.score;

import com.google.common.net.HostAndPort;
import io.triada.dates.DateConverters;

import java.time.Duration;
import java.util.Date;


public final class TriadaScore implements Score {

    /**
     * Max amount of hours a score can stay fresh
     */
    private static final int BEST_BEFORE = 24;

    /**
     * Score time
     */
    private final Date time;

    /**
     * Host and port
     */
    private final HostAndPort hostAndPort;

    /**
     * Invoice // TODO: 1/26/19 Replace it with two values
     */
    private final String invoice;

    /**
     * Array of suffixes
     */
    private final String[] suffixes;

    /**
     * Amount of zeros
     */
    private final int strength;

    /**
     * Created at time
     */
    private final Date created;

    public TriadaScore(
            final Date time,
            final HostAndPort hostAndPort,
            final String invoice,
            final String[] suffixes,
            final int strength,
            final Date created
    ) {
        this.time = time;
        this.hostAndPort = hostAndPort;
        this.invoice = invoice;
        this.suffixes = suffixes;
        this.strength = strength;
        this.created = created;
    }

    @Override
    public Score next() {
        if (this.expired(BEST_BEFORE)) {
            final Date now = new Date();
            return new TriadaScore(
                    now,
                    this.hostAndPort,
                    this.invoice,
                    new String[]{},
                    this.strength,
                    now
            );
        } else {

        }
        return null;
    }

    /**
     * @param hours Hours value
     * @return True if the age of the score is over 24 hours
     */
    private boolean expired(final int hours) {
        return this.age() > hours * 60 * 60;
    }

    /**
     * @return Prefix for the hash calc
     */
    private String prefix() {
        return String.format(
                "%d %s %d %s",
                this.time.getTime(),
                this.hostAndPort.getHost(),
                this.hostAndPort.getPort(),
                this.invoice
        );
    }

    /**
     * @return Age of score in seconds
     */
    private long age() {
        return Duration.between(DateConverters.toLocalDateTime(this.time), DateConverters.toLocalDateTime(new Date())).getSeconds();
    }
}
