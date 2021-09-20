package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.exception.BadRequestException;
import org.apache.commons.text.WordUtils;
import org.junit.platform.commons.util.StringUtils;

import java.util.Deque;
import java.util.Iterator;

public class ConverterUtils {

    private ConverterUtils() {}

    public static String navigatePath(Deque<String> path) {
        if (path == null) {
            throw new BadRequestException("Please verify the path argument");
        }

        return navigatePath(path, path.size());
    }

    public static String navigatePath(Deque<String> path, int depth) {
        if (path == null) {
            throw new BadRequestException("Please verify the path argument");
        }

        if (depth < 0) {
            throw new BadRequestException("The depth argument has to be greater than zero");
        }

        StringBuilder absolutePath = new StringBuilder();
        Iterator<String> itr = path.iterator();

        // Root
        if (itr.hasNext()) {
            absolutePath.append(WordUtils.uncapitalize(itr.next()));
        }

        int count = 1;
        // Children
        while (itr.hasNext()) {
            if (count >= depth) {
                return absolutePath.toString();
            }

            absolutePath.append(".");
            absolutePath.append(WordUtils.uncapitalize(itr.next()));
            count++;
        }

        return absolutePath.toString();
    }

    public static String formatSchemaName(String schemaName) {
        if (StringUtils.isBlank(schemaName)) {
            throw new BadRequestException("Please provide a non-empty string for the schemaName parameter.");
        }

        if (!schemaName.contains(".avsc")) {
            schemaName += ".avsc";
        }

        return schemaName.toLowerCase();
    }
}
