package bio.ferlab.fhir.schema.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolUtils {

    private static final String FIRST_CHARACTER_REGEX = "[A-Za-z_]";
    private static final String SUBSEQUENT_CHARACTER_REGEX = "[A-Za-z0-9_]*";
    private static final String REGEX_NAMING_CONVENTION = FIRST_CHARACTER_REGEX + SUBSEQUENT_CHARACTER_REGEX;
    private static final List<Character> INVALID_FIRST_CHARACTERS = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    private static final List<String> INVALID_CHARACTERS = Arrays.asList("-", ".", "/", "<=", "<", ">=", ">", "!=", "=");

    public static String toHexString(char character) {
        return "H00" + Integer.toHexString(character).toUpperCase();
    }

    public static String toHexString(String string) {
        StringBuilder encodedSymbol = new StringBuilder();
        for (char character : string.toCharArray()) {
            encodedSymbol.append(toHexString(character));
        }
        return encodedSymbol.toString();
    }

    public static String fromHexString(String hex) {
        StringBuilder decodedSymbol = new StringBuilder();
        Arrays.stream(hex.split("H"))
                .filter(x -> !x.isEmpty())
                .forEach(x -> decodedSymbol.append((char) Integer.parseUnsignedInt(x, 16)));
        return decodedSymbol.toString();
    }

    // symbols: a JSON array, listing symbols, as JSON strings (required).
    // All symbols in an enum must be unique; duplicates are prohibited.
    // Every symbol must match the regular expression [A-Za-z_][A-Za-z0-9_]* (the same requirement as for names).
    public static String encodeSymbol(String symbol) {
        char firstCharacter = symbol.charAt(0);
        if (INVALID_FIRST_CHARACTERS.contains(firstCharacter)) {
            symbol = symbol.replace(Character.toString(firstCharacter), toHexString(firstCharacter));
        }

        if (!Pattern.compile(REGEX_NAMING_CONVENTION).matcher(symbol).matches()) {
            for (String invalidCharacter : INVALID_CHARACTERS) {
                if (symbol.contains(invalidCharacter)) {
                    symbol = symbol.replace(invalidCharacter, toHexString(invalidCharacter));
                }
            }
        }
        return symbol;
    }

    public static String decodeSymbol(String symbol) {
        Matcher matcher = Pattern.compile("H00..").matcher(symbol);
        while (matcher.find()) {
            String group = matcher.group();
            String decodedGroup = fromHexString(group);
            symbol = symbol.replace(group, decodedGroup);
        }
        return symbol;
    }
}
