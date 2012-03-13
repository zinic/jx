package net.jps.jx.jackson;

import com.rackspace.papi.components.limits.schema.Limits;
import com.rackspace.papi.components.limits.schema.ObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import net.jps.jx.jackson.mapping.ObjectGraphBuilder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
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
         
         final JsonFactory jsonFactory = new JsonFactory();
         final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(baos);

         final JxJsonWriter jsonWriter = new JxJsonWriter(jsonGenerator, limits);
         
         try {
            jsonWriter.write();
            jsonGenerator.close();
         } catch (Throwable ex) {
            ex.printStackTrace(System.out);
         }
         
         final JsonParser jsonParser = jsonFactory.createJsonParser(new ByteArrayInputStream(baos.toByteArray()));
         final JxJsonReader<Limits> limitsReader = new JxJsonReader<Limits>(jsonParser, Limits.class);
         final ObjectGraphBuilder<Limits> builder = limitsReader.render();
         
         System.out.println("Done. Built object: " + builder.getObjectInstance());
      }

      public Limits unwrap(Object o) {
         if (o instanceof JAXBElement) {
            return ((JAXBElement<Limits>) o).getValue();
         }

         return (Limits) o;
      }
   }
}
