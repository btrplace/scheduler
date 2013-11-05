package btrplace.solver.api.cstrSpec;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.Reader;

/**
 * @author Fabien Hermenier
 */
public class UnitTestsExecutor {

    private Constraint cstr;

    public UnitTestsExecutor(Reader in) throws ParseException {
        JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
        JSONObject o = (JSONObject) p.parse(in);
    }



}
