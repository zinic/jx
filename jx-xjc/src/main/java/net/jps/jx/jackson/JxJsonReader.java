package net.jps.jx.jackson;

import java.io.IOException;
import java.util.Stack;
import net.jps.jx.jackson.mapping.ObjectGraphBuilder;
import net.jps.jx.jackson.mapping.ObjectGraphNode;
import net.jps.jx.jackson.mapping.MappedCollection;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author zinic
 */
public class JxJsonReader<T> {

   private final JsonParser jsonParser;
   private final ObjectGraphBuilder<T> graphTrunk;

   public JxJsonReader(JsonParser jsonParser, Class<T> objectGraphClass) {
      this.jsonParser = jsonParser;

      this.graphTrunk = ObjectGraphBuilder.builderFor(objectGraphClass);
   }

   public ObjectGraphBuilder<T> render() throws IOException {
      final Stack<ObjectGraphNode> nodeStack = new Stack<ObjectGraphNode>();

      ObjectGraphNode currentGraphNode = new ObjectGraphNode(graphTrunk, null), selectedField = null;

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
                  currentGraphNode = new ObjectGraphNode(mappedCollection.newCollectionValue(), null);
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
               selectedField.getMappedField().set(null);
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

               break;
         }
      }

      return graphTrunk;
   }

   public void setOrAdd(ObjectGraphNode currentGraphNode, Object builtObjectInstance) {
      if (currentGraphNode.getMappedField() != null) {
         if (currentGraphNode.isCollection()) {
            ((MappedCollection) currentGraphNode.getMappedField()).add(builtObjectInstance);
         } else {
            currentGraphNode.getMappedField().set(builtObjectInstance);
         }
      }
   }
}
