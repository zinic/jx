package net.jps.jx.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import net.jps.jx.JsonReader;
import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.mapping.FieldMapper;
import net.jps.jx.jackson.mapping.ObjectGraphBuilder;
import net.jps.jx.jackson.mapping.ReaderGraphNode;
import net.jps.jx.jackson.mapping.MappedCollection;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author zinic
 */
public class JacksonJsonReader<T> implements JsonReader<T> {

   private final FieldMapper fieldMapper;
   private final JsonFactory jsonFactory;
   private final ObjectGraphBuilder<T> graphTrunk;

   public JacksonJsonReader(JsonFactory jsonFactory, FieldMapper fieldMapper, Class<T> objectGraphClass) {
      this.jsonFactory = jsonFactory;
      this.fieldMapper = fieldMapper;

      this.graphTrunk = ObjectGraphBuilder.builderFor(objectGraphClass, fieldMapper);
   }

   @Override
   public T read(InputStream source) throws IOException, JxParsingException {
      final JsonParser newParser = jsonFactory.createJsonParser(source);
      
      return render(newParser).getObjectInstance();
   }

   public ObjectGraphBuilder<T> render(JsonParser jsonParser) throws IOException, JxParsingException {
      final Stack<ReaderGraphNode> nodeStack = new Stack<ReaderGraphNode>();

      ReaderGraphNode currentGraphNode = new ReaderGraphNode(graphTrunk, null), selectedField = null;

      JsonToken jsonToken;

      while ((jsonToken = jsonParser.nextToken()) != null) {
         switch (jsonToken) {
            case FIELD_NAME:
               selectedField = currentGraphNode.getObjectBuilder().getField(jsonParser.getText());

               break;

            case START_OBJECT:
               if (currentGraphNode.shouldWrap()) {
                  currentGraphNode.setWrapping(true);
               } else if (currentGraphNode.isCollection()) {
                  // Moving to the next element in the collection
                  final MappedCollection mappedCollection = (MappedCollection) currentGraphNode.getMappedField();

                  nodeStack.push(currentGraphNode);
                  
                  final ObjectGraphBuilder collectionValueBuilder = ObjectGraphBuilder.builderFor(mappedCollection.getCollectionValueClass(), fieldMapper);
                  currentGraphNode = new ReaderGraphNode(collectionValueBuilder, null);
               } else {
                  // Moving to the next field
                  nodeStack.push(currentGraphNode);
                  currentGraphNode = selectedField;
               }

               break;

            case END_OBJECT:
               if (currentGraphNode.isWrapped()) {
                  currentGraphNode.setWrapping(false);
               } else {
                  final Object builtObjectInstance = currentGraphNode.getObjectBuilder().getObjectInstance();

                  if (currentGraphNode.getMappedField() != null) {
                     setOrAdd(currentGraphNode, builtObjectInstance);
                  } else {
                     currentGraphNode = nodeStack.pop();
                     setOrAdd(currentGraphNode, builtObjectInstance);
                  }
               }

               break;

            case START_ARRAY:
               if (!selectedField.isCollection()) {
                  throw new RuntimeException("Unexpected collection start.");
               }

               nodeStack.push(currentGraphNode);
               currentGraphNode = selectedField;

               break;

            case END_ARRAY:
               if (currentGraphNode.isCollection()) {
                  currentGraphNode = nodeStack.pop();
               } else {
                  throw new RuntimeException("Unexpected collection end.");
               }

               break;

            case VALUE_TRUE:
               selectedField.getMappedField().set(Boolean.TRUE);
               break;

            case VALUE_FALSE:
               selectedField.getMappedField().set(Boolean.FALSE);
               break;

            case VALUE_NULL:
               if (selectedField != null && selectedField.getMappedField() != null) {
                  selectedField.getMappedField().set(null);
               } else {
                  System.out.println("TODO:Fix - Failure case trap. This is happening because of how nulls are outputted. No nulls, no problem.");
               }
               
               break;

            case VALUE_NUMBER_FLOAT:
               selectedField.getMappedField().set(Float.valueOf(jsonParser.getFloatValue()));
               break;

            case VALUE_NUMBER_INT:
               selectedField.getMappedField().set(Integer.valueOf(jsonParser.getIntValue()));
               break;

            case VALUE_STRING:
               selectedField.getMappedField().set(jsonParser.getText());
               break;

            case VALUE_EMBEDDED_OBJECT:
               throw new UnsupportedOperationException("Embedded object encountered. Log this as a bug with the a sample of your input, please.");

            case NOT_AVAILABLE:
               throw new JxParsingException("Unavilable. Log this as a bug with the a sample of your input, please.");
         
         }
      }

      return graphTrunk;
   }

   public void setOrAdd(ReaderGraphNode currentGraphNode, Object builtObjectInstance) {
      if (currentGraphNode.getMappedField() != null) {
         if (currentGraphNode.isCollection()) {
            ((MappedCollection) currentGraphNode.getMappedField()).add(builtObjectInstance);
         } else {
            currentGraphNode.getMappedField().set(builtObjectInstance);
         }
      }
   }
}
