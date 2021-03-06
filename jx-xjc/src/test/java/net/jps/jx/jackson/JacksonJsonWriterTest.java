package net.jps.jx.jackson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import net.jps.jx.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import net.jps.jx.mapping.reflection.DefaultClassMapper;
import org.codehaus.jackson.JsonFactory;

import static net.jps.jx.jackson.TestClasses.*;
import net.jps.jx.mapping.DefaultObjectConstructor;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class JacksonJsonWriterTest {

    @Ignore
    public static class TestParent {

        protected JsonFactory jsonFactory;
        protected JxControls jxControls;

        @Before
        public void standUp() throws DatatypeConfigurationException {
            jsonFactory = new JsonFactory();
            jxControls = new JxControlsImpl(new DefaultObjectConstructor(DatatypeFactory.newInstance()), DefaultClassMapper.getInstance());
        }
    }

    public static class WhenWritingSimpleObjects extends TestParent {

        @Test
        public void shouldProduceJsonThatMapps1to1Correctly() throws Exception {
            final MultiFieldMixedAnnotations expected = new MultiFieldMixedAnnotations();
            expected.setDefault("default");
            expected.setJsonNumber(4);
            expected.setStringField("field");
            expected.setXmlDouble(2.4);

            final JxFactory factory = new JacksonJxFactory();
            final JsonWriter<MultiFieldMixedAnnotations> jsonWriter = factory.newWriter(MultiFieldMixedAnnotations.class);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jsonWriter.write(expected, baos);

            final JsonReader<MultiFieldMixedAnnotations> jsonReader = factory.newReader(MultiFieldMixedAnnotations.class);
            final MultiFieldMixedAnnotations rendered = jsonReader.read(new ByteArrayInputStream(baos.toByteArray()));

            assertEquals("", expected.getDefault(), rendered.getDefault());
            assertEquals("", expected.getJsonNumber(), rendered.getJsonNumber());
            assertEquals("", expected.getStringField(), rendered.getStringField());
            assertEquals("", Double.valueOf(expected.getXmlDouble()), Double.valueOf(rendered.getXmlDouble()));
        }
    }
}
