package bio.ferlab.fhir.converter.converters;

import bio.ferlab.fhir.converter.DateUtils;

public class DateConverter implements IConverter<String> {

    @Override
    public String convert(String value) {
        return Long.toString(DateUtils.toEpochDay(value));
    }

    @Override
    public String getPattern() {
        return "^([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?$";
    }
}
