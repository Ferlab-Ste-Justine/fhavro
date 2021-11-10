package fixture;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.MarkdownType;

import java.util.Date;

public class AnnotationFixture {

    public static Annotation createAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setTime(new Date());
        annotation.setText("Text!");
        return annotation;
    }
}
