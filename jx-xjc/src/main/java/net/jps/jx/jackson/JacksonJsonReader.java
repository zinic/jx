package net.jps.jx.jackson;

import java.io.IOException;
import java.io.InputStream;
import net.jps.jx.JsonReader;
import net.jps.jx.JxParsingException;
import net.jps.jx.mapping.FieldMapper;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

/**
 *
 * @author zinic
 */
public class JacksonJsonReader<T> implements JsonReader<T> {

   private final FieldMapper fieldMapper;
   private final JsonFactory jsonFactory;
   private final Class<T> graphTrunkClass;

   public JacksonJsonReader(JsonFactory jsonFactory, FieldMapper fieldMapper, Class<T> graphTrunkClass) {
      this.jsonFactory = jsonFactory;
      this.fieldMapper = fieldMapper;

      this.graphTrunkClass = graphTrunkClass;
   }

   @Override
   public T read(InputStream source) throws IOException, JxParsingException {
      final JsonParser newParser = jsonFactory.createJsonParser(source);
      final JsonGraphReader<T> jsonGraphReader = new JsonGraphReader<T>(graphTrunkClass, fieldMapper);

      return jsonGraphReader.render(newParser).getFieldOwnerInstance();
   }
}
