package bio.ferlab.fhir.converter.converters;

public interface IConverter<T> {

    T convert(String value);

    String getPattern();
}
