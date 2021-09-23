import bio.ferlab.fhir.schema.utils.SymbolUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SymbolUtilsTest {

    private final Map<String, String> SYMBOL_RESULTS = new HashMap<>() {{
        put("<=", "H003CH003D");
        put(">=", "H003EH003D");
        put("!=", "H0021H003D");
        put("=", "H003D");
        put("1", "H0031");
        put("1.0.0", "H0031H002E0H002E0");
        put("OLDAP-2.2.2", "OLDAPH002D2H002E2H002E2");
        put("OSET-PL-2.1", "OSETH002DPLH002D2H002E1");
        put("W3C-19980720", "W3CH002D19980720");
        put("temporarily-closed-to-accrual-and-intervention", "temporarilyH002DclosedH002DtoH002DaccrualH002DandH002Dintervention");
    }};

    @Test
    public void test_toHexString_with_character() {
        assertEquals("H003C", SymbolUtils.toHexString('<'));
    }

    @Test
    public void test_toHexString_with_string() {
        assertEquals("H003CH003D", SymbolUtils.toHexString("<="));
    }

    @Test
    public void test_fromHexString() {
        assertEquals("<", SymbolUtils.fromHexString("H003C"));
    }

    @Test
    public void test_encodeSymbol() {
        for (Map.Entry<String, String> entry : SYMBOL_RESULTS.entrySet()) {
            assertEquals(entry.getValue(), SymbolUtils.encodeSymbol(entry.getKey()));
        }
    }

    @Test
    public void test_decodeSymbol() {
        for (Map.Entry<String, String> entry : SYMBOL_RESULTS.entrySet()) {
            assertEquals(entry.getKey(), SymbolUtils.decodeSymbol(entry.getValue()));
        }
    }
}
