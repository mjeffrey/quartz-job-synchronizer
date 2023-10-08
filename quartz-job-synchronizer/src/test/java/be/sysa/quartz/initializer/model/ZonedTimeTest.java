package be.sysa.quartz.initializer.model;

import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.util.EnumSet;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ZonedTimeTest {

    public static final LocalTime MORNING = LocalTime.of(1, 2, 3);
    public static final LocalTime ANYTIME = LocalTime.of(12, 34, 56);
    public static final ZoneId LONDON = ZoneId.of("Europe/London");
    public static final ZoneOffset UTC = ZoneOffset.UTC;

    @ParameterizedTest
    @MethodSource("parsing")
    @DisplayName("Test a set of parseable ZonedTime strings")
    void parseZonedDateTime(Expectation testExpectation) {
        ZonedTime actual = ZonedTime.parse(testExpectation.getFormatted());
        ZonedTime expected = testExpectation.getZonedTime();

        assertThat(actual).isEqualTo(expected);
        assertThat(actual)
                .returns(expected.getLocalTime(), ZonedTime::getLocalTime)
                .returns(expected.getZone(), ZonedTime::getZone);
    }

    @ParameterizedTest
    @MethodSource("formatWithSeconds")
    @DisplayName("Test a set of ZonedTime when formatted with seconds")
    void formatZonedDateTimeWithSeconds(Expectation testExpectation) {
        String actual = ZonedTime.format(testExpectation.getZonedTime());
        assertThat(actual).isEqualTo(testExpectation.getFormatted());
    }

    @ParameterizedTest
    @MethodSource("supportedFields")
    @DisplayName("Supported Temporal Fields")
    public void supportedFields(ChronoField chronoField) {
        ZonedTime anyTime = ZonedTime.parse("01:02:03Z");
        assertThat(anyTime.isSupported(chronoField)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("unsupportedFields")
    @DisplayName("Unsupported Temporal Fields")
    public void unsupportedFields(ChronoField chronoField) {
        ZonedTime anyTime = ZonedTime.parse("01:02:03Z");
        assertThat(anyTime.isSupported(chronoField)).isFalse();
    }

    @Test
    @DisplayName("When passing a ZonedTime to from(), just return the value.")
    public void fromTemporalAccessorZonedTime() {
        ZonedTime any = ZonedTime.parse("01:02:03Z");
        assertThat(ZonedTime.from(any)).isSameAs(any);
    }
    @Test
    @DisplayName("When passing an Instant to from(), we get an exception since thee is no associated timezone")
    public void fromTemporalAccessorInstant() {
        assertThatExceptionOfType(DateTimeException.class)
                .isThrownBy( ()->ZonedTime.from(Instant.now()));
    }

    @Test
    @DisplayName("When passing an Instant to from(), we get an exception since thee is no associated timezone")
    public void fromTemporalAccessorZonedDataTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
        ZonedTime zonedTime = ZonedTime.from(zonedDateTime);
        assertThat(zonedTime)
                .returns(zonedDateTime.getZone(), ZonedTime::getZone)
                .returns(LocalTime.from(zonedDateTime), ZonedTime::getLocalTime)
        ;
    }

    @ParameterizedTest
    @MethodSource("supportedQueries")
    @DisplayName("Supported Temporal queries")
    public void supportedQueries(TemporalQuery<?> query) {
        ZonedTime anyTime = ZonedTime.parse("01:02:03Z");
        assertThat(anyTime.query(query)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("unsupportedQueries")
    @DisplayName("Unsupported Temporal queries")
    public void unsupportedQueries(TemporalQuery<?> query) {
        ZonedTime anyTime = ZonedTime.parse("01:02:03Z");
        assertThat(anyTime.query(query)).isNull();
    }

    public static Stream<TemporalQuery<?>> supportedQueries() {
        return Stream.of(
                TemporalQueries.zone(),
                TemporalQueries.zoneId(),
                TemporalQueries.localTime()
        );
    }
    public static Stream<TemporalQuery<?>> unsupportedQueries() {
        return Stream.of(
                TemporalQueries.chronology(),
                TemporalQueries.localDate(),
                TemporalQueries.offset(),
                TemporalQueries.precision()
        );
    }

    public static EnumSet<ChronoField> supportedFields() {
        return EnumSet.of(
                ChronoField.HOUR_OF_DAY,
                ChronoField.SECOND_OF_MINUTE,
                ChronoField.MINUTE_OF_HOUR,
                ChronoField.OFFSET_SECONDS
        );
    }
    public static EnumSet<ChronoField> unsupportedFields() {
        return EnumSet.complementOf(supportedFields());
    }


    private static Stream<Expectation> parsing() {
        return Stream.of(
                new Expectation(ZonedTime.of(MORNING, LONDON), "01:02:03 Europe/London"),
                new Expectation(ZonedTime.of(MORNING, UTC), "01:02:03 Z"),
                new Expectation(ZonedTime.of(MORNING, UTC), "01:02:03Z"),

                new Expectation(ZonedTime.of(ANYTIME, LONDON), "12:34:56 Europe/London"),
                new Expectation(ZonedTime.of(ANYTIME, UTC), "12:34:56 Z"),
                new Expectation(ZonedTime.of(ANYTIME, UTC), "12:34:56Z"),
                new Expectation(ZonedTime.of(ANYTIME.truncatedTo(MINUTES), LONDON), "12:34 Europe/London"),
                new Expectation(ZonedTime.of(ANYTIME.truncatedTo(MINUTES), UTC), "12:34 Z"),
                new Expectation(ZonedTime.of(ANYTIME.truncatedTo(MINUTES), UTC), "12:34Z")
        );
    }

    private static Stream<Expectation> formatWithSeconds() {
        return Stream.of(
                new Expectation(ZonedTime.of(ANYTIME, LONDON), "12:34:56 Europe/London"),
                new Expectation(ZonedTime.of(ANYTIME, UTC), "12:34:56Z")
        );
    }

    @Value
    private static class Expectation {
        ZonedTime zonedTime;
        String formatted;
    }
}
