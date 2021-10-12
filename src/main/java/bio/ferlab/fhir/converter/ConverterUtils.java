package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.exception.BadRequestException;
import org.apache.commons.text.WordUtils;
import org.hl7.fhir.r4.model.Base;
import org.junit.platform.commons.util.StringUtils;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class ConverterUtils {

    private ConverterUtils() {
    }

    public static String navigatePath(Deque<String> path) {
        if (path == null) {
            throw new BadRequestException("Please verify the path argument");
        }

        return navigatePath(path, true, path.size(), 0);
    }

    public static String navigatePath(Deque<String> path, int depth) {
        return navigatePath(path, true, depth, 0);
    }

    public static String navigatePath(Deque<String> path, boolean forward, int depth) {
        return navigatePath(path, forward, depth, 0);
    }

    public static String navigatePath(Deque<String> path, boolean forward, int depth, int skip) {
        if (path == null) {
            throw new BadRequestException("Please verify the path argument");
        }

        if (depth < 0) {
            throw new BadRequestException("The depth argument has to be greater than zero");
        }

        StringBuilder absolutePath = new StringBuilder();
        Iterator<String> itr = (forward) ? path.iterator() : path.descendingIterator();

        for (int i = 0; i < skip; i++) {
            if (itr.hasNext()) {
                itr.next();
            }
        }

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

    public static Base getBase(List<Base> bases) {
        return Optional.ofNullable(bases.get(0))
                .orElseThrow(() -> new RuntimeException("Please verify this, this isn't suppose to occur."));
    }
}
