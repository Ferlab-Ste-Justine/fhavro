package bio.ferlab.fhir.converter.converters;

import bio.ferlab.fhir.converter.DateUtils;

public class DateTimeConverter implements IConverter<String> {

    @Override
    public String convert(String value) {
        return Long.toString(DateUtils.toEpochSecond(value));
    }

    @Override
    public String getPattern() {
        return "^([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\\\.[0-9]+)?(Z|(\\\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?$";
    }
}
