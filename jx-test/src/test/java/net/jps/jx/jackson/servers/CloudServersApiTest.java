package net.jps.jx.jackson.servers;

import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBElement;
import net.jps.jx.JsonWriter;
import net.jps.jx.JxFactory;
import net.jps.jx.jackson.JacksonJxFactory;
import net.jps.jx.test.papi.components.limits.schema.Limits;
import net.jps.jx.test.rackspace.cloud.servers.api.Metadata;
import net.jps.jx.test.rackspace.cloud.servers.api.MetadataItem;
import net.jps.jx.test.rackspace.cloud.servers.api.Server;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class CloudServersApiTest {

   public static class WhenSerializingJxJaxbObjects {

      @Test
      public void should() throws Exception {
//         final InputStream inputStream = JxJsonWriterTest.class.getResourceAsStream("/META-INF/schema/limits/example.xml");

//         final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
//         final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

         final Server server = new Server();
         
         final Metadata metadata = new Metadata();
         final MetadataItem item = new MetadataItem();
         item.setKey("key");
         item.setValue("Value");
         
         metadata.getMeta().add(item);
         server.setMetadata(metadata);

         final ByteArrayOutputStream baos = new ByteArrayOutputStream();

         final JxFactory jxFactory = new JacksonJxFactory();
         final JsonWriter<Server> jsonWriter = jxFactory.newWriter(Server.class);

         try {
            jsonWriter.write(server, baos);
         } catch (Throwable ex) {
            ex.printStackTrace(System.out);
         }

         System.out.println("JSON Output\n" + new String(baos.toByteArray()) + "\n");
      }

      public Limits unwrap(Object o) {
         if (o instanceof JAXBElement) {
            return ((JAXBElement<Limits>) o).getValue();
         }

         return (Limits) o;
      }
   }
}
