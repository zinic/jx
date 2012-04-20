package net.jps.jx.jackson;

import java.io.IOException;
import java.io.InputStream;
import net.jps.jx.JsonReader;
import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.mapping.ClassCrawler;
import net.jps.jx.mapping.ClassDescriptor;
import net.jps.jx.JxControls;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

/**
 *
 * @author zinic
 */
public class JacksonJsonReader<T> implements JsonReader<T> {

    private final JsonFactory jsonFactory;
    private final ClassDescriptor<T> trunkClassDescriptor;
    private final JxControls jxControls;
    
    JacksonJsonReader(Class<T> trunkClass, JsonFactory jsonFactory, JxControls jxControls) {
        this.jsonFactory = jsonFactory;
        this.jxControls = jxControls;
        
        final ClassCrawler crawler = new ClassCrawler(jxControls.getClassMapper(), trunkClass);
        trunkClassDescriptor = crawler.getGraph();
    }

    @Override
    public T read(InputStream source) throws IOException, JxParsingException {
        final JsonParser newParser = jsonFactory.createJsonParser(source);

        return new JsonGraphReader<T>(trunkClassDescriptor, jxControls.getClassMapper(), jxControls.getObjectConstructor()).render(newParser);
    }
}
