package net.jps.jx.jackson;

import com.rackspace.papi.components.limits.schema.Limits;
import com.rackspace.papi.components.limits.schema.ObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import net.jps.jx.JsonReader;
import net.jps.jx.JsonWriter;
import net.jps.jx.JxControls;
import net.jps.jx.JxControlsImpl;
import net.jps.jx.mapping.DefaultObjectConstructor;
import net.jps.jx.mapping.reflection.StaticFieldMapper;
import org.codehaus.jackson.JsonFactory;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class JxJsonReaderTest {

    public static class WhenSerializingJxJaxbObjects {

        @Test
        public void should() throws Exception {
            final InputStream inputStream = JxJsonReaderTest.class.getResourceAsStream("/META-INF/xml/limits.xml");

            final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            final Limits limits = unwrap(unmarshaller.unmarshal(inputStream));

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            final JxControls jxControls = new JxControlsImpl(new DefaultObjectConstructor(DatatypeFactory.newInstance()), StaticFieldMapper.getInstance());
            final JsonFactory jsonFactory = new JsonFactory();
            final JsonWriter jsonWriter = new JacksonJsonWriter(jsonFactory, jxControls);

            try {
                jsonWriter.write(limits, baos);
            } catch (Throwable ex) {
                ex.printStackTrace(System.out);
            }

            final JsonReader<Limits> limitsReader = new JacksonJsonReader<Limits>(jsonFactory, jxControls, Limits.class);
            final Limits bidirectionaledLimits = limitsReader.read(new ByteArrayInputStream(baos.toByteArray()));

            System.out.println("Done. Built object: " + bidirectionaledLimits);
        }

        public Limits unwrap(Object o) {
            if (o instanceof JAXBElement) {
                return ((JAXBElement<Limits>) o).getValue();
            }

            return (Limits) o;
        }
    }
}
