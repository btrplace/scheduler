package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.JSONUtils;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class CTestsCaseInput implements Iterator<CTestCase>, Iterable<CTestCase> {

    private JsonReader reader;

    private Gson gson;

    public CTestsCaseInput(InputStream in) throws IOException {
        gson = JSONUtils.getInstance().getGson();
        reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginArray();

    }

    @Override
    public Iterator<CTestCase> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        try {
            return reader.hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public CTestCase next() {
        return gson.fromJson(reader, CTestCase.class);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
