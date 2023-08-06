package be.sysa.quartz.initializer.model;

import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

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
                .returns(expected.getZoneId(), ZonedTime::getZoneId);
    }

    @ParameterizedTest
    @MethodSource("formatWithSeconds")
    @DisplayName("Test a set of ZonedTime when formatted with seconds")
    void formatZonedDateTimeWithSeconds(Expectation testExpectation) {
        String actual = ZonedTime.format(testExpectation.getZonedTime());
        assertThat(actual).isEqualTo(testExpectation.getFormatted());
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
