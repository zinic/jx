package net.jps.jx.jackson.plugin.impl.fieldmapping;

import java.io.InputStream;
import javax.xml.datatype.DatatypeConfigurationException;
import net.jps.jx.JsonReader;
import net.jps.jx.JxFactory;
import net.jps.jx.jackson.JacksonJsonReaderTest;
import net.jps.jx.jackson.JacksonJxFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;


import static org.junit.Assert.*;
import static net.jps.jx.jackson.plugin.impl.fieldmapping.TestClasses.*;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class SequenceRemappingMapperTest {

   @Ignore
   public static class TestParent {

      protected JxFactory factory;

      @Before
      public void standUp() throws DatatypeConfigurationException {
         factory = new JacksonJxFactory();
      }
   }

   public static class WhenMappingCollections extends TestParent {

      @Test
      public void shouldMakeMagicHappen() throws Exception {
         final SequenceMappedAsObject smao = new SequenceMappedAsObject();

         MappingElement mappingElement = new MappingElement();
         mappingElement.setKey("test1");
         mappingElement.setValue("1value");
         smao.getMappings().add(mappingElement);

         mappingElement = new MappingElement();
         mappingElement.setKey("test2");
         mappingElement.setValue("2value");
         smao.getMappings().add(mappingElement);

         mappingElement = new MappingElement();
         mappingElement.setKey("test3");
         mappingElement.setValue("3value");
         smao.getMappings().add(mappingElement);

         mappingElement = new MappingElement();
         mappingElement.setKey("test4");
         mappingElement.setValue("4value");
         smao.getMappings().add(mappingElement);


         final JsonReader<SequenceMappedAsObject> jsonReader = factory.newReader(SequenceMappedAsObject.class);

         final InputStream inputStream = JacksonJsonReaderTest.class.getResourceAsStream("/META-INF/json/SequenceMappedAsJsonHash.json");
         final SequenceMappedAsObject materializedObject = jsonReader.read(inputStream);

         assertEquals("Must have 4 elements.", 4, materializedObject.getMappings().size());
         assertMappingElementIs(materializedObject.getMappings().get(0), "test1", "1value");
         assertMappingElementIs(materializedObject.getMappings().get(1), "test2", "2value");
         assertMappingElementIs(materializedObject.getMappings().get(2), "test3", "3value");
         assertMappingElementIs(materializedObject.getMappings().get(3), "test4", "4value");
      }
      
      private void assertMappingElementIs(MappingElement element, String key, String value) {
         assertEquals("Key must match expected.", key, element.getKey());
         assertEquals("Value must match expected.", value, element.getValue());
      }
   }
}
