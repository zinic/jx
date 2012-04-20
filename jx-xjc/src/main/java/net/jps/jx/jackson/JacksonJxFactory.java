package net.jps.jx.jackson;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import net.jps.jx.*;
import net.jps.jx.mapping.DefaultObjectConstructor;
import net.jps.jx.mapping.reflection.DefaultClassMapper;
import org.codehaus.jackson.JsonFactory;

/**
 *
 * @author zinic
 */
public class JacksonJxFactory implements JxFactory {

    private final JsonFactory jsonFactory;
    private final JxControls jxControls;

    public JacksonJxFactory() throws DatatypeConfigurationException {
        this(new JsonFactory(), new JxControlsImpl(new DefaultObjectConstructor(DatatypeFactory.newInstance()), DefaultClassMapper.getInstance()));
    }

    public JacksonJxFactory(JsonFactory jsonFactory, JxControls jxControls) {
        this.jsonFactory = jsonFactory;
        this.jxControls = jxControls;
    }

    @Override
    public <T> JsonReader<T> newReader(Class<T> c) {
        return new JacksonJsonReader<T>(c, jsonFactory, jxControls);
    }

    @Override
    public <T> JsonWriter<T> newWriter(Class<T> c) {
        return new JacksonJsonWriter<T>(c, jsonFactory, jxControls);
    }
}
