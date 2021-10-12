package bio.ferlab.fhir.schema.profile;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Element;

public class Cardinality {

    private Integer min;
    private String max;

    private boolean valid;

    public Cardinality(Element element) {
        org.hl7.fhir.r4.model.Property minProperty = element.getNamedProperty("min");
        if (minProperty != null && minProperty.hasValues()) {
            Base minBase = minProperty.getValues().get(0);
            setMin(minBase.castToUnsignedInt(minBase).getValue());
        } else {
            setMin(0);
        }

        org.hl7.fhir.r4.model.Property maxProperty = element.getNamedProperty("max");
        if (maxProperty != null && maxProperty.hasValues()) {
            Base maxBase = maxProperty.getValues().get(0);
            setMax(maxBase.castToString(maxBase).getValue());
            setValid(true);
        } else {
            setValid(false);
        }
    }

    public boolean toBeRemoved() {
        return hasMax() && "0".equals(getMax()) && getMin() == 0;
    }

    public boolean isRequired() {
        return hasMax() && getMin() >= 1;
    }

    public boolean isArray() {
        return hasMax() && ("*".equals(getMax()) || Integer.parseInt(getMax()) > 1);
    }

    public boolean isOptional() {
        return hasMax() && !"0".equals(getMax()) && getMin() == 0;
    }

    public boolean hasMax() {
        return StringUtils.isNotBlank(this.max);
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
