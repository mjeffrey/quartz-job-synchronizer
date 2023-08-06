package be.sysa.quartz.initializer.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.util.Objects;
import java.util.Set;

import static java.time.temporal.ChronoField.*;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class ZonedTime implements TemporalAccessor {
    private static final Set<TemporalField> SUPPORTED_FIELDS = Set.of(SECOND_OF_MINUTE, MINUTE_OF_HOUR, HOUR_OF_DAY, OFFSET_SECONDS );
    public static final DateTimeFormatter PARSER = DateTimeFormatter.ofPattern("HH:mm[:ss][ VV][X]");
    public static final DateTimeFormatter FORMATTER_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss VV");
    public static final DateTimeFormatter FORMATTER_SECONDS_UTC = DateTimeFormatter.ofPattern("HH:mm:ssX");
    public static final DateTimeFormatter FORMATTER_MINUTES = DateTimeFormatter.ofPattern("HH:mm VV");

    private final LocalTime localTime;
    private final ZoneId zoneId;

    public static String format(ZonedTime zonedTime) {
        DateTimeFormatter formatter = ZoneOffset.UTC.equals(zonedTime.getZoneId()) ? FORMATTER_SECONDS_UTC : FORMATTER_SECONDS;
        return formatter.format(zonedTime);
    }
    public static ZonedTime of(LocalTime localTime, ZoneId zoneId) {
        return new ZonedTime(localTime, zoneId);
    }

    public static ZonedTime parse(CharSequence text) {
        return parse(text, PARSER);
    }

    public static ZonedTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, ZonedTime::from);
    }

    public static ZonedTime from(TemporalAccessor temporal) {
        if (temporal instanceof ZonedTime) {
            return (ZonedTime) temporal;
        }
        try {
            ZoneId zone = ZoneId.from(temporal);
            LocalTime time = LocalTime.from(temporal);
            return new ZonedTime(time, zone);
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ZonedTime from TemporalAccessor: " +
                    temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }
    @Override
    public boolean isSupported(TemporalField field) {
        return SUPPORTED_FIELDS.contains(field);
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zone() || query == TemporalQueries.zoneId()) {
            return (R) zoneId;

        } else if (query == TemporalQueries.localTime()) {
            return (R) localTime;
        }
        return null;
    }

    @Override
    public long getLong(TemporalField field) {
        if ( field == OFFSET_SECONDS){
            return zoneId.getRules().getStandardOffset(null).getLong(field);
        }
        return localTime.getLong(field);
    }
}
