package be.sysa.quartz.initializer.support;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.spi.MutableTrigger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneRules;
import java.util.Date;
import java.util.TimeZone;

public class ScheduleTimezoneVerifierTest {
    // TODO implemetn timezone checks
    @SneakyThrows
    @Test
    public void name() {
        String cronOriginal = "0 30 2 ? * MON,TUE,WED,THU,FRI *";
        DstCalc dstCalc = DstCalc.builder().zoneIdString("Europe/Brussels").cronOriginal(cronOriginal).build();
        Instant nextDate1 = dstCalc.testDate(Instant.now());
        System.out.println("----------");
        Instant nextDate2 = dstCalc.testDate(nextDate1);

//        System.out.println(nextDate2);
    }


    @Value
    public static class DstCalc {
        ZoneId zoneId;
        MutableTrigger schedule;
        ZoneRules zoneRules;

        @SneakyThrows
        @Builder
        public DstCalc(String zoneIdString, String cronOriginal) {

            this.zoneId = ZoneId.of(zoneIdString);
            zoneRules = zoneId.getRules();
            String[] strings = cronOriginal.split(" ");
            String cronExpression = String.format("%s %s %s ? * * *", strings[0], strings[1], strings[2]);

            schedule = CronScheduleBuilder.cronSchedule(cronExpression)
                    .inTimeZone(TimeZone.getTimeZone(zoneId)).build();
        }

        Instant testDate(Instant instant) {
            Instant testDay = nextTransitionDay(instant);
            System.out.println(ZonedDateTime.ofInstant(testDay, zoneId));

            Instant nextFire1 = getNextFire(testDay);
            System.out.println(ZonedDateTime.ofInstant(nextFire1, zoneId));

            Instant nextFire2 = getNextFire(nextFire1);
            System.out.println(ZonedDateTime.ofInstant(nextFire2, zoneId));

            return nextTransitionDay(testDay.plus(1, ChronoUnit.DAYS));
        }

        private Instant getNextFire(Instant currentInstant) {
            return schedule.getFireTimeAfter(Date.from(currentInstant)).toInstant();
        }

        public Instant nextTransitionDay(Instant instant) {
            Instant transition = zoneRules.nextTransition(instant).getInstant();
            return startOfDay(transition);
        }

        private Instant startOfDay(Instant instant) {
            return ZonedDateTime.ofInstant(instant, zoneId).truncatedTo(ChronoUnit.DAYS).toInstant();
        }
    }

}
