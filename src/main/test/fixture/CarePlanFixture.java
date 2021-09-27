package fixture;

import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.StringType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarePlanFixture {

    public static CarePlan createCarePlan() {
        CarePlan carePlan = new CarePlan();
        List<CodeableConcept> categories = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            categories.add(CodeableConceptFixture.createCodeableConcept(4));
        }
        carePlan.setCategory(categories);
        carePlan.setStatus(CarePlan.CarePlanStatus.ONHOLD);
        carePlan.setTitle("The Best Care Plan Ever");
        carePlan.setPeriod(PeriodFixture.createOngoingPeriod());
        carePlan.setCreated(new Date());
        carePlan.setAuthor(ReferenceFixture.createAbsoluteReference());

        List<CarePlan.CarePlanActivityComponent> carePlanActivityComponents = new ArrayList<>();
        CarePlan.CarePlanActivityComponent carePlanActivityComponent = new CarePlan.CarePlanActivityComponent();
        carePlanActivityComponent.setOutcomeCodeableConcept(List.of(CodeableConceptFixture.createCodeableConcept()));
        carePlanActivityComponent.setProgress(List.of(AnnotationFixture.createAnnotation()));
        carePlanActivityComponent.setReference(ReferenceFixture.createAbsoluteReference());

        CarePlan.CarePlanActivityDetailComponent carePlanActivityDetailComponent = new CarePlan.CarePlanActivityDetailComponent();
        carePlanActivityDetailComponent.setKind(CarePlan.CarePlanActivityKind.DEVICEREQUEST);
        carePlanActivityDetailComponent.setStatusReason(CodeableConceptFixture.createCodeableConcept());
        carePlanActivityDetailComponent.setLocationTarget(LocationFixture.createLocation());

        // To see if both are going to be serialized. OR that one overwrite the other even thought they are different type.
        carePlanActivityDetailComponent.setScheduled(PeriodFixture.createOngoingPeriod());
        carePlanActivityDetailComponent.setScheduled(new StringType("This is a string!"));

        carePlanActivityComponent.setDetail(carePlanActivityDetailComponent);

        carePlan.setActivity(carePlanActivityComponents);
        return carePlan;
    }
}
