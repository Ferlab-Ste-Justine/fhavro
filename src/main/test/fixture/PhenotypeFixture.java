package fixture;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import java.util.Date;
import java.util.List;

public class PhenotypeFixture {

    public static Condition createCondition() {
        Condition condition = new Condition();

        condition.setId("phenotype-example-1");
        condition.setMeta(new Meta().addProfile("https://ncpi-fhir.github.io/ncpi-fhir-ig/StructureDefinition/phenotype"));

        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setId("id");
        XhtmlNode xhtmlNode = new XhtmlNode();
        xhtmlNode.setValueAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative</b></p><p><b>verificationStatus</b>: <span title=\"Codes: {http://terminology.hl7.org/CodeSystem/condition-ver-status confirmed}\">Confirmed</span></p><p><b>code</b>: <span title=\"Codes: {http://purl.obolibrary.org/obo/hp.owl HP:0000076}\">Present: Vesicoureteral reflux</span></p><p><b>subject</b>: <a href=\"Patient-patient-example-1.html\">Generated Summary: Mariah Abigail Smith(OFFICIAL); Phone: (555) 555-5555; gender: female; birthDate: 2019-06-15</a></p><p><b>recordedDate</b>: </p></div>");
        narrative.setDiv(xhtmlNode);
        condition.setText(narrative);

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setCoding(List.of(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status").setCode("confirmed").setDisplay("Confirmed")));
        condition.setVerificationStatus(codeableConcept);

        CodeableConcept code = new CodeableConcept();
        code.setCoding(List.of(new Coding().setSystem("http://purl.obolibrary.org/obo/hp.owl").setCode("HP:0000076").setDisplay("Vesicoureteral reflux")));
        code.setText("Present: Vesicoureteral reflux");
        condition.setCode(code);
        condition.setSubject(new Reference().setReference("Patient/patient-example-1"));

        // OHO HO
        condition.setRecordedDate(new Date());
        return condition;
    }
    /*
    {
      "_recordedDate" : {
        "extension" : [
          {
            "extension" : [
              {
                "url" : "target",
                "valueReference" : {
                  "reference" : "Patient/patient-example-1"
                }
              },
              {
                "url" : "targetPath",
                "valueString" : "birthDate"
              },
              {
                "url" : "relationship",
                "valueCode" : "after"
              },
              {
                "url" : "offset",
                "valueDuration" : {
                  "value" : 2,
                  "unit" : "w",
                  "system" : "http://unitsofmeasure.org",
                  "code" : "weeks"
                }
              }
            ],
            "url" : "http://hl7.org/fhir/StructureDefinition/cqf-relativeDateTime"
          }
        ]
      }
    }
    */
}
