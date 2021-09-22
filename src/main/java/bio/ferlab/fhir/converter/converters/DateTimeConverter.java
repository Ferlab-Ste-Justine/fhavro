package bio.ferlab.fhir.converter.converters;

public class DateTimeConverter extends DateConverter {

    @Override
    public String convert(String value) {
        return Long.toString(parseDate(value, getFormat())
                .toInstant()
                .getEpochSecond());
    }

    @Override
    public String getPattern() {
        return "^([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\\\.[0-9]+)?(Z|(\\\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?$";
    }

    @Override
    public String getFormat() {
        return "yyyy-MM-dd'T'HH:mm:ssXXX";
    }
}
