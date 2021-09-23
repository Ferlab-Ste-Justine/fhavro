import bio.ferlab.fhir.converter.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
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
        assertEquals(java.util.Optional.of(1632401377L).get(), DateUtils.toEpochSecond("2021-09-23T08:49:37-04:00"));
    }
}
