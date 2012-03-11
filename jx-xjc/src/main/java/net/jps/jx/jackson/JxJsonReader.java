package net.jps.jx.jackson;

import java.io.IOException;
import java.util.Stack;
import net.jps.jx.util.reflection.JxAnnotationTool;
import net.jps.jx.jackson.mapping.ObjectGraphBuilder;
import net.jps.jx.jackson.mapping.ObjectGraphNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author zinic
 */
public class JxJsonReader<T> {

   private static final String JAXB_DEFAULT_NAME = "##default";
   private final JsonParser jsonParser;
   private final Class<T> objectGraphClass;
   private final ObjectGraphBuilder objectGraph;
   private ObjectGraphNode currentNode;
   private String currentFieldName;

   public JxJsonReader(JsonParser jsonParser, Class<T> objectGraphClass) {
      this.jsonParser = jsonParser;
      this.objectGraphClass = objectGraphClass;

      this.objectGraph = ObjectGraphBuilder.builderFor(objectGraphClass);
   }

   public T parse() throws IOException {
      final Stack<ObjectGraphNode> nodeStack = new Stack<ObjectGraphNode>();

      while (jsonParser.hasCurrentToken()) {
         final JsonToken token = jsonParser.nextToken();

         switch (token) {
            case FIELD_NAME:
               jsonParser.getText();
               
            case START_OBJECT:
               if (JxAnnotationTool.shouldWrapClass(objectGraphClass)) {
               }

            case END_OBJECT:

            case START_ARRAY:
            case END_ARRAY:

            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
            case VALUE_STRING:
               
            case VALUE_EMBEDDED_OBJECT:
               nodeStack.push(currentNode);
         }
      }


      return null;
   }
}
