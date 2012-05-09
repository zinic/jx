package net.jps.jx.jackson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import net.jps.jx.*;
import net.jps.jx.test.papi.components.limits.schema.Limits;
import net.jps.jx.test.papi.components.limits.schema.ObjectFactory;
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
            final InputStream inputStream = JxJsonReaderTest.class.getResourceAsStream("/META-INF/schema/limits/example.xml");

            final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            final Limits limits = unwrap(unmarshaller.unmarshal(inputStream));

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            final JxFactory jxFactory = new JacksonJxFactory();
            final JsonWriter<Limits> jsonWriter = jxFactory.newWriter(Limits.class);

            try {
                jsonWriter.write(limits, baos);
            } catch (Throwable ex) {
                ex.printStackTrace(System.out);
            }

            final JsonReader<Limits> limitsReader = jxFactory.newReader(Limits.class);
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
