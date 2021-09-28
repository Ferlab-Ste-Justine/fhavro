package fixture;

import org.hl7.fhir.r4.model.Annotation;

import java.util.Date;

public class AnnotationFixture {

    public static Annotation createAnnotation() {
        Annotation annotation = new Annotation();
        // annotation.setAuthor(); what the hell is a Type ?
        annotation.setTime(new Date());
        annotation.setText("Text!");
        return annotation;
    }
}
