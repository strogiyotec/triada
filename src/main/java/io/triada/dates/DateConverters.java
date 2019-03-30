package io.triada.dates;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

// TODO: 3/10/19 utility class is bad
@UtilityClass
public final class DateConverters {

    public LocalDate toLocalDate(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public LocalDateTime toLocalDateTime(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public String asIso(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        return sdf.format(date);
    }

    public Date fromIso(final String iso) {
        return Date.from(OffsetDateTime.parse(iso).toInstant());
    }

    public Date nowMinusYears(final int years) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1 * years);
        return cal.getTime();
    }

    public Date nowMinusHours(final int hours) {
        final Instant ins = Instant.now().minus(hours, ChronoUnit.HOURS);
        return Date.from(ins);
    }

    public boolean isUnix(final String unix) {
        try {
            return new Date().getTime() - Long.parseLong(unix) >= 0;
        } catch (final NumberFormatException exc) {
            return false;
        }
    }
}
