package net.jps.jx.jackson;

import java.io.IOException;
import java.io.OutputStream;
import net.jps.jx.JsonWriter;
import net.jps.jx.JxControls;
import net.jps.jx.JxWritingException;
import net.jps.jx.jackson.mapping.ClassCrawler;
import org.codehaus.jackson.JsonFactory;

/**
 *
 * @author zinic
 */
public class JacksonJsonWriter<T> implements JsonWriter<T> {

    private final ClassCrawler rootClassCrawler;
    private final JsonFactory jsonFactory;
    private final JxControls jxControls;

    JacksonJsonWriter(Class<T> clazz, JsonFactory jsonFactory, JxControls jxControls) {
        this.rootClassCrawler = new ClassCrawler(clazz);
        this.jsonFactory = jsonFactory;
        this.jxControls = jxControls;
    }
    
    @Override
    public void write(T rootObject, OutputStream outputStream) throws IOException, JxWritingException {
        new JsonGraphWriter(jsonFactory, jxControls.getClassMapper(), rootClassCrawler.getGraph()).write(rootObject, outputStream);
    }
}
