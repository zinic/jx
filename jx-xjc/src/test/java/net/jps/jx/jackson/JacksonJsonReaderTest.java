package net.jps.jx.jackson;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import net.jps.jx.JxControlsImpl;
import net.jps.jx.JxParsingException;
import net.jps.jx.mapping.reflection.StaticFieldMapper;
import org.codehaus.jackson.JsonFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static net.jps.jx.jackson.TestClasses.*;
import net.jps.jx.mapping.DefaultObjectConstructor;
import org.junit.Ignore;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class JacksonJsonReaderTest {
    
    @Ignore
    public static class TestParent {
        
        protected JsonFactory jsonFactory;
        protected JxControlsImpl jxControls;
        
        @Before
        public void standUp() throws DatatypeConfigurationException {
            jsonFactory = new JsonFactory();
            jxControls = new JxControlsImpl(new DefaultObjectConstructor(DatatypeFactory.newInstance()), StaticFieldMapper.getInstance());
        }
    }
    
    public static class WhenReadingSimpleObjectGraphs extends TestParent {
        
        @Test
        public void shouldReadStringFields() throws Exception {
            final JacksonJsonReader<OneStringField> jsonReader = new JacksonJsonReader<OneStringField>(jsonFactory, jxControls, OneStringField.class);
            
            final ByteArrayInputStream inputStream = new ByteArrayInputStream("{ \"stringField\": \"test\" }".getBytes());
            final OneStringField materializedObject = jsonReader.read(inputStream);
            
            assertEquals("Json string field must be mapped and set correctly.", "test", materializedObject.getStringField());
        }
        
        @Test(expected = JxParsingException.class) @Ignore
        public void shouldCatchUnexpectedCollections() throws Exception {
            final JacksonJsonReader<OneStringField> jsonReader = new JacksonJsonReader<OneStringField>(jsonFactory, jxControls, OneStringField.class);
            
            final ByteArrayInputStream inputStream = new ByteArrayInputStream("{ \"stringField\": [ \"test\" ]}".getBytes());
            final OneStringField materializedObject = jsonReader.read(inputStream);
            
            assertEquals("Json string field must be mapped and set correctly.", "test", materializedObject.getStringField());
        }
        
        @Test @Ignore
        public void shouldReadAnnotatedFields() throws Exception {
            final JacksonJsonReader<MultiFieldMixedAnnotations> jsonReader = new JacksonJsonReader<MultiFieldMixedAnnotations>(jsonFactory, jxControls, MultiFieldMixedAnnotations.class);
            
            final InputStream inputStream = JacksonJsonReaderTest.class.getResourceAsStream("/META-INF/json/MultiFieldMixedAnnotations.json");
            final MultiFieldMixedAnnotations materializedObject = jsonReader.read(inputStream);
            
            assertEquals("", "remapped-string-field", materializedObject.getDefault());
            assertEquals("", Integer.valueOf(1), materializedObject.getJsonNumber());
            assertEquals("", Double.valueOf(2.4), Double.valueOf(materializedObject.getXmlDouble()));
            assertEquals("", "default-string-field", materializedObject.getStringField());
        }
        
        @Test @Ignore
        public void shouldReadAnnotatedFieldsWithPartialJsonPayload() throws Exception {
            final JacksonJsonReader<MultiFieldMixedAnnotations> jsonReader = new JacksonJsonReader<MultiFieldMixedAnnotations>(jsonFactory, jxControls, MultiFieldMixedAnnotations.class);
            
            final InputStream inputStream = JacksonJsonReaderTest.class.getResourceAsStream("/META-INF/json/MultiFieldMixedAnnotations_MissingField.json");
            final MultiFieldMixedAnnotations materializedObject = jsonReader.read(inputStream);
            
            assertNull("", materializedObject.getDefault());
            assertEquals("", Integer.valueOf(1), materializedObject.getJsonNumber());
            assertEquals("", Double.valueOf(2.4), Double.valueOf(materializedObject.getXmlDouble()));
            assertEquals("", "default-string-field", materializedObject.getStringField());
        }
        
        @Test @Ignore
        public void shouldSkipExtraFields() throws Exception {
            final JacksonJsonReader<MultiFieldMixedAnnotations> jsonReader = new JacksonJsonReader<MultiFieldMixedAnnotations>(jsonFactory, jxControls, MultiFieldMixedAnnotations.class);
            
            final InputStream inputStream = JacksonJsonReaderTest.class.getResourceAsStream("/META-INF/json/MultiFieldMixedAnnotations_ExtraField.json");
            final MultiFieldMixedAnnotations materializedObject = jsonReader.read(inputStream);
            
            assertEquals("", "remapped-string-field", materializedObject.getDefault());
            assertEquals("", Integer.valueOf(1), materializedObject.getJsonNumber());
            assertEquals("", Double.valueOf(2.4), Double.valueOf(materializedObject.getXmlDouble()));
            assertEquals("", "default-string-field", materializedObject.getStringField());
        }
        
        @Test @Ignore
        public void shouldSkipExtraComplexFields() throws Exception {
            final JacksonJsonReader<MultiFieldMixedAnnotations> jsonReader = new JacksonJsonReader<MultiFieldMixedAnnotations>(jsonFactory, jxControls, MultiFieldMixedAnnotations.class);
            
            final InputStream inputStream = JacksonJsonReaderTest.class.getResourceAsStream("/META-INF/json/MultiFieldMixedAnnotations_ExtraFieldComplex.json");
            final MultiFieldMixedAnnotations materializedObject = jsonReader.read(inputStream);
            
            assertEquals("", "remapped-string-field", materializedObject.getDefault());
            assertEquals("", Integer.valueOf(1), materializedObject.getJsonNumber());
            assertEquals("", Double.valueOf(2.4), Double.valueOf(materializedObject.getXmlDouble()));
            assertEquals("", "default-string-field", materializedObject.getStringField());
        }
    }
    
    public static class WhenReadingObjectGraphsWithCollections extends TestParent {
        
        @Test @Ignore
        public void shouldMapCollections() throws Exception {
            final JacksonJsonReader<CollectionFields> jsonReader = new JacksonJsonReader<CollectionFields>(jsonFactory, jxControls, CollectionFields.class);
            
            final InputStream inputStream = JacksonJsonReaderTest.class.getResourceAsStream("/META-INF/json/CollectionFields.json");
            final CollectionFields materializedObject = jsonReader.read(inputStream);
            
            assertNotNull("", materializedObject.getStringFields());
            assertEquals("", 5, materializedObject.getStringFields().size());
            
            for (OneStringField osf : materializedObject.getStringFields()) {
                assertNotNull("", osf.getStringField());
            }
        }
    }
}
