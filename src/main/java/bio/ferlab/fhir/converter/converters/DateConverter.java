package bio.ferlab.fhir.converter.converters;

import bio.ferlab.fhir.converter.exception.AvroConversionException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

public class DateConverter implements IConverter<String> {

    public String convert(String value) {
        return Long.toString(parseDate(value, getFormat())
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay());
    }

    public String getPattern() {
        return "^([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?$";
    }

    public String getFormat() {
        return "yyyy-MM-dd";
    }

    // Move this somewhere else ?
    protected static Date parseDate(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException parseException) {
            throw new AvroConversionException(String.format("value: %s is unparseable according to format: %s", date, format));
        }
    }
}
