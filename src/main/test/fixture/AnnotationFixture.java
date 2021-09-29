package fixture;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.DateType;

import java.util.Date;

public class AnnotationFixture {

    public static Annotation createAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setAuthor(new DateType(new Date()));
        annotation.setTime(new Date());
        annotation.setText("Text!");
        return annotation;
    }
}
