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

/**
 * A class representing a ZonedTime, which combines a LocalTime and a ZoneId.
 *
 * <p>Instances of {@code ZonedTime} are immutable, which means that once created, their values cannot be changed.
 * Instances of this class are guaranteed to be valid and not-null.
 *
 * <p>This class implements the {@code TemporalAccessor} interface, providing access to the time and zone information.
 * It also provides various utility methods for formatting, parsing, and obtaining values from a {@code TemporalAccessor}.
 *
 * <p>The supported fields for this class are: SECOND_OF_MINUTE, MINUTE_OF_HOUR, HOUR_OF_DAY, and OFFSET_SECONDS.
 *
 * <p>Example usage:
 * <pre>
 * LocalTime localTime = LocalTime.now();
 * ZoneId zoneId = ZoneId.systemDefault();
 * ZonedTime zonedTime = ZonedTime.of(localTime, zoneId);
 * </pre>
 */
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class ZonedTime implements TemporalAccessor {
    private static final Set<TemporalField> SUPPORTED_FIELDS = Set.of(SECOND_OF_MINUTE, MINUTE_OF_HOUR, HOUR_OF_DAY, OFFSET_SECONDS );
    private static final DateTimeFormatter PARSER = DateTimeFormatter.ofPattern("HH:mm[:ss][ VV][X]");
    private static final DateTimeFormatter FORMATTER_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss VV");
    private static final DateTimeFormatter FORMATTER_SECONDS_UTC = DateTimeFormatter.ofPattern("HH:mm:ssX");
    private static final DateTimeFormatter FORMATTER_MINUTES = DateTimeFormatter.ofPattern("HH:mm VV");

    private final LocalTime localTime;
    private final ZoneId zone;

    /**
     * Formats the given ZonedTime object into a string representation.
     *
     * @param zonedTime the ZonedTime object to format
     * @return the formatted string representation of the ZonedTime object
     */
    public static String format(ZonedTime zonedTime) {
        DateTimeFormatter formatter = ZoneOffset.UTC.equals(zonedTime.getZone()) ? FORMATTER_SECONDS_UTC : FORMATTER_SECONDS;
        return formatter.format(zonedTime);
    }
    /**
     * Creates a new ZonedTime object with the specified LocalTime and ZoneId.
     *
     * @param localTime the LocalTime representing the time portion
     * @param zoneId the ZoneId representing the time zone
     * @return a new ZonedTime object initialized with the specified LocalTime and ZoneId
     */
    public static ZonedTime of(LocalTime localTime, ZoneId zoneId) {
        return new ZonedTime(localTime, zoneId);
    }

    /**
     * Parses the specified CharSequence to create a new ZonedTime object.
     *
     * @param text the text to parse, not null
     * @return a new ZonedTime object parsed from the specified text
     */
    public static ZonedTime parse(CharSequence text) {
        return parse(text, PARSER);
    }

    /**
     * Parses the specified CharSequence using the provided DateTimeFormatter to create a new ZonedTime object.
     *
     * @param text the text to parse, not null
     * @param formatter the DateTimeFormatter to use for parsing, not null
     * @return a new ZonedTime object parsed from the specified text
     * @throws NullPointerException if the formatter is null
     */
    public static ZonedTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, ZonedTime::from);
    }

    /**
     * Creates a new ZonedTime object from the specified TemporalAccessor.
     * If the specified TemporalAccessor is already an instance of ZonedTime, it is simply cast and returned.
     * Otherwise, it attempts to extract the ZoneId and LocalTime from the TemporalAccessor and create a new ZonedTime object.
     *
     * @param temporal the TemporalAccessor from which to create the ZonedTime object
     * @return a new ZonedTime object created from the specified TemporalAccessor
     * @throws DateTimeException if unable to obtain ZonedTime from TemporalAccessor
     */
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
            return (R) zone;

        } else if (query == TemporalQueries.localTime()) {
            return (R) localTime;
        }
        return null;
    }

    @Override
    public long getLong(TemporalField field) {
        if ( field == OFFSET_SECONDS){
            return zone.getRules().getStandardOffset(null).getLong(field);
        }
        return localTime.getLong(field);
    }
}
