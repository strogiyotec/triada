package io.triada.models.score;

import com.google.common.net.HostAndPort;
import io.triada.commands.remote.RemoteNodes;
import io.triada.dates.DateConverters;
import io.triada.models.hash.BigIntegerHash;
import io.triada.models.hash.Hash;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


public final class TriadaScore implements Score {


    /**
     * Max amount of hours a score can stay fresh
     */
    public static final int BEST_BEFORE = 24;

    /**
     * Default amount of zeroes for Hash
     */
    public static final int STRENGTH = 8;

    /**
     * Zero score
     */
    public static final TriadaScore ZERO =
            new TriadaScore(
                    HostAndPort.fromParts("localhost", RemoteNodes.PORT),
                    "NOPREFIX@ffffffffffffffff",
                    STRENGTH
            );

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
    private final List<String> suffixes;

    /**
     * Amount of zeros
     */
    private final int strength;

    /**
     * Created at time
     */
    private final Date created;

    /**
     * @param body Text to parse into Score
     */
    public TriadaScore(final String body) {
        final String[] parts = body.split(" ");

        this.strength = Integer.parseInt(parts[0]);
        this.time = new Date(Long.parseLong(parts[1]));
        this.hostAndPort = HostAndPort.fromParts(parts[2], Integer.parseInt(parts[3]));
        this.invoice = parts[4];
        if (parts.length == 6) {
            this.suffixes = Arrays.asList(parts[5].split("_"));
        } else {
            this.suffixes = Collections.emptyList();
        }
        this.created = new Date();

    }

    /**
     * Ctor
     */
    public TriadaScore(
            final Date time,
            final HostAndPort hostAndPort,
            final String invoice,
            final List<String> suffixes,
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

    /**
     * Ctor
     */
    public TriadaScore(
            final Date time,
            final HostAndPort hostAndPort,
            final String invoice,
            final List<String> suffixes,
            final Date created
    ) {
        this.time = time;
        this.hostAndPort = hostAndPort;
        this.invoice = invoice;
        this.suffixes = suffixes;
        this.strength = STRENGTH;
        this.created = created;
    }

    /**
     * Ctor
     */
    public TriadaScore(
            final Date time,
            final HostAndPort hostAndPort,
            final String invoice,
            final int strength,
            final Date created
    ) {
        this.time = time;
        this.hostAndPort = hostAndPort;
        this.invoice = invoice;
        this.suffixes = Collections.emptyList();
        this.strength = strength;
        this.created = created;
    }

    /**
     * Ctor
     */
    public TriadaScore(
            final Date time,
            final HostAndPort hostAndPort,
            final String invoice,
            final int strength
    ) {
        this.time = time;
        this.hostAndPort = hostAndPort;
        this.invoice = invoice;
        this.suffixes = Collections.emptyList();
        this.strength = strength;
        this.created = new Date();
    }

    /**
     * Ctor
     */
    public TriadaScore(
            final HostAndPort hostAndPort,
            final String invoice,
            final int strength
    ) {
        final Date now = new Date();

        this.time = now;
        this.hostAndPort = hostAndPort;
        this.invoice = invoice;
        this.suffixes = Collections.emptyList();
        this.strength = strength;
        this.created = now;
    }

    /**
     * Ctor
     */
    public TriadaScore(
            final HostAndPort hostAndPort,
            final String invoice,
            final List<String> suffixes
    ) {
        final Date now = new Date();

        this.time = now;
        this.hostAndPort = hostAndPort;
        this.invoice = invoice;
        this.suffixes = suffixes;
        this.created = now;
        this.strength = STRENGTH;
    }

    @Override
    public String mnemo() {
        final SimpleDateFormat format = new SimpleDateFormat("HHmm");
        return String.format(
                "%d:%s",
                this.value(),
                format.format(this.time)
        );
    }

    /**
     * @return New score with one more suffix
     */
    @Override
    public Score next() {
        if (this.expired(BEST_BEFORE)) {
            final Date now = new Date();
            return new TriadaScore(
                    now,
                    this.hostAndPort,
                    this.invoice,
                    Collections.emptyList(),
                    this.strength,
                    now
            );
        }
        final String suffix =
                new BigIntegerHash(
                        this.suffixes.isEmpty() ? this.prefix() : hash(),
                        this.strength
                ).nonce();
        return new TriadaScore(
                this.time,
                this.hostAndPort,
                this.invoice,
                this.oldSuffixesWithNewOne(suffix),
                this.strength,
                this.created
        );
    }

    @Override
    public List<String> suffixes() {
        return this.suffixes;
    }

    @Override
    public Date createdAt() {
        return this.created;
    }

    @Override
    public HostAndPort address() {
        return this.hostAndPort;
    }

    @Override
    public String invoice() {
        return this.invoice;
    }

    @Override
    public int strength() {
        return this.strength;
    }

    @Override
    public Date time() {
        return this.time;
    }

    @Override
    public String hash() {
        if (this.suffixes.isEmpty()) {
            throw new IllegalStateException("Score has zero value , there is no prefix");
        }
        return this.suffixes.stream().reduce(this.prefix(), Hash::sha256);
    }

    /**
     * @return Amount of prefixes
     */
    @Override
    public int value() {
        return this.suffixes.size();
    }

    /**
     * @param hours Hours value
     * @return Expired if wallet older than given hours (usually 24)
     */
    @Override
    public boolean expired(final int hours) {
        return this.age() > hours * 60 * 60;
    }

    @Override
    public boolean valid() {
        return (this.suffixes().isEmpty() || this.hash().endsWith(String.join("", Collections.nCopies(this.strength(), "0"))))
                && this.time().compareTo(new Date()) < 0;
    }

    /**
     * @return Prefix for the hash calculation
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
    @Override
    public long age() {
        return Duration.between(DateConverters.toLocalDateTime(this.time), DateConverters.toLocalDateTime(new Date())).getSeconds();
    }

    /**
     * Because by default suffixes is {@link java.util.Collections.EmptyList}
     * we need to create new one providing old suffixes and add new to them
     *
     * @param suffix New Suffix
     * @return new list of suffixes
     */
    private List<String> oldSuffixesWithNewOne(final String suffix) {
        final List<String> suffixes = new ArrayList<>(this.suffixes);
        suffixes.add(suffix);
        return suffixes;
    }

    @Override
    public String asText() {
        return String.format(
                "%d %d %s %d %s %s",
                this.strength,
                this.time.getTime(),
                this.hostAndPort.getHost(),
                this.hostAndPort.getPort(),
                this.invoice,
                this.suffixes.stream().collect(Collectors.joining("_"))
        );
    }
}
