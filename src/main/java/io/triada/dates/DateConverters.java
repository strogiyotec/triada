package io.triada.dates;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

@UtilityClass
public final class DateConverters {

    public LocalDate toLocalDate(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public LocalDateTime toLocalDateTime(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public Date nowMinusYears(final int years) {
        final Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -1 * years);
        return cal.getTime();
    }

    public Date nowMinusHours(final int hours) {
        final Instant ins = Instant.now().minus(hours, ChronoUnit.HOURS);
        return Date.from(ins);
    }
}
