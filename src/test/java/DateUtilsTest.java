import bio.ferlab.fhir.converter.DateUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DateUtilsTest {

    @Test
    public void test_formatTimestampMicros() {
        assertEquals("2021-09-23T12:49:37Z", DateUtils.formatTimestampMicros(1632401377L));
    }

    @Test
    public void test_formatDate() {
        assertEquals("2021-09-23", DateUtils.formatDate(18893));
    }

    @Test
    public void test_toEpochDay() {
        assertEquals(java.util.Optional.of(18893L).get(), DateUtils.toEpochDay("2021-09-23"));
    }

    @Test
    public void test_toEpochSecond() {
        assertEquals(java.util.Optional.of(1574935896L).get(), DateUtils.toEpochSecond("2019-11-28T10:11:36"));
    }

    @Test
    public void test_toEpochSecond_with_offset() {
        assertEquals(java.util.Optional.of(1632386977L).get(), DateUtils.toEpochSecond("2021-09-23T08:49:37-04:00"));
    }

    @Test
    public void test_toEpochSecond_with_microseconds() {
        assertEquals(java.util.Optional.of(1574935896L).get(), DateUtils.toEpochSecond("2019-11-28T10:11:36.928"));
    }

    @Test
    public void test_toEpochSecond_with_microseconds_with_offset() {
        assertEquals(java.util.Optional.of(1574935896L).get(), DateUtils.toEpochSecond("2019-11-28T10:11:36.928+00:00"));
    }
}
