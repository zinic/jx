package net.jps.jx.jackson;

import java.io.ByteArrayInputStream;
import net.jps.jx.mapping.reflection.StaticFieldMapper;
import org.codehaus.jackson.JsonFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static net.jps.jx.jackson.TestClasses.*;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class JacksonJsonReaderTest {
   public static class WhenReadingSimpleObjectGraphs {

      protected JsonFactory jsonFactory;

      @Before
      public void standUp() {
         jsonFactory = new JsonFactory();
      }
      
      @Test
      public void shouldReadStringFields() throws Exception {
         final JacksonJsonReader<OneStringField> jsonReader = new JacksonJsonReader<OneStringField>(jsonFactory, StaticFieldMapper.getInstance(), OneStringField.class);
         
         final ByteArrayInputStream inputStream = new ByteArrayInputStream("{ \"stringField\": \"test\" }".getBytes());
         final OneStringField materializedField = jsonReader.read(inputStream);
         
         assertEquals("", "test", materializedField.getStringField());
      }
   }
}
