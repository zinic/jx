package net.jps.jx.jackson;

import net.jps.jx.mapping.ClassMapper;
import java.io.IOException;
import java.io.OutputStream;
import net.jps.jx.JsonWriter;
import net.jps.jx.JxControls;
import net.jps.jx.JxWritingException;
import org.codehaus.jackson.JsonFactory;

/**
 *
 * @author zinic
 */
public class JacksonJsonWriter<T> implements JsonWriter<T> {

    private final JsonFactory jsonFactory;
    private final JxControls jxControls;

    public JacksonJsonWriter(JsonFactory jsonFactory, JxControls jxControls) {
        this.jsonFactory = jsonFactory;
        this.jxControls = jxControls;
    }
    @Override
    public void write(T rootObject, OutputStream outputStream) throws IOException, JxWritingException {
        new JsonGraphWriter(jsonFactory, jxControls.getClassMapper()).write(rootObject, outputStream);
    }
}
