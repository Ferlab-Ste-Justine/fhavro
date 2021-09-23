package bio.ferlab.fhir.converter;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private DateUtils() {
    }

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public static ZonedDateTime parseTimestamp(String timestamp) {
        return ZonedDateTime.parse(timestamp, DATETIME_FORMATTER);
    }

    public static String formatDate(Integer epochDay) {
        return LocalDate.ofEpochDay(epochDay).format(DATE_FORMATTER);
    }

    public static String formatTimestampMicros(Long epochSeconds) {
        // Avro long, where the long stores the number of microseconds from the unix epoch, 1 January 1970 00:00:00.000000 UTC.
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of("UTC")).format(DATETIME_FORMATTER);
    }

    public static Long toEpochSecond(String timestamp) {
        return LocalDateTime.ofInstant(parseTimestamp(timestamp).toInstant(), ZoneId.of("UTC"))
                .toInstant(ZoneOffset.UTC)
                .getEpochSecond();
    }

    public static Long toEpochDay(String date) {
        return DateUtils.parseDate(date).toEpochDay();
    }
}
