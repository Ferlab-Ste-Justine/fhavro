package fixture;


import org.hl7.fhir.r4.model.Period;

import java.util.Date;

public class PeriodFixture {

    public static Period createOngoingPeriod() {
        return new Period().setStart(new Date());
    }

    public static Period createPlannedPeriod() {
        return new Period()
                .setStart(new Date())
                .setEnd(new Date());
    }
}
