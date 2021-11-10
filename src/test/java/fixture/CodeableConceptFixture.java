package fixture;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import java.util.ArrayList;
import java.util.List;

public class CodeableConceptFixture {

    public static Coding createCoding(String code) {
        return new Coding()
                .setDisplay("Coding")
                .setCode(code)
                .setSystem("This is a system");
    }

    public static CodeableConcept createCodeableConcept() {
        return createCodeableConcept(4);
    }

    public static CodeableConcept createCodeableConcept(int size) {
        List<Coding> codings = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            codings.add(createCoding(String.valueOf(i)));
        }
        return new CodeableConcept()
                .setText("CodeableConcept")
                .setCoding(codings);
    }
}
