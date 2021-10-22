package bio.ferlab.fhir.converter;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter GENERIC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd[['T']HH:mm:ss[.SSS][XXX]]");

    private DateUtils() {
    }

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public static String formatDate(Integer epochDay) {
        return LocalDate.ofEpochDay(epochDay).format(DATE_FORMATTER);
    }

    public static String formatTimestampMicros(Long epochSeconds) {
        // Avro long, where the long stores the number of microseconds from the unix epoch, 1 January 1970 00:00:00.000000 UTC.
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of("UTC")).format(DATETIME_FORMATTER);
    }

    public static String formatTimestampMillis(Long epochMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis * 1000), ZoneId.of("UTC")).format(UTC_FORMATTER);
    }

    public static Long toEpochSecond(String timestamp) {
        return LocalDateTime.parse(timestamp, GENERIC_FORMATTER).toInstant(ZoneOffset.UTC).toEpochMilli() / 1000;
    }

    public static Long toEpochDay(String date) {
        return DateUtils.parseDate(date).toEpochDay();
    }
}
