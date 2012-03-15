package net.jps.jx.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import net.jps.jx.JsonReader;
import net.jps.jx.JxParsingException;
import net.jps.jx.mapping.FieldMapper;
import net.jps.jx.jackson.mapping.ReaderGraphNodeValue;
import net.jps.jx.jackson.mapping.ReaderGraphNode;
import net.jps.jx.mapping.MappedCollection;
import net.jps.jx.util.reflection.ReflectionException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public class JacksonJsonReader<T> implements JsonReader<T> {

   private static final Logger LOG = LoggerFactory.getLogger(JacksonJsonReader.class);
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

      return render(newParser).getFieldOwnerInstance();
   }

   public ReaderGraphNodeValue<T> render(JsonParser jsonParser) throws IOException, JxParsingException {
      final Stack<ReaderGraphNode> graphNodeStack = new Stack<ReaderGraphNode>();
      final ReaderGraphNodeValue<T> graphTrunk = ReaderGraphNodeValue.builderFor(graphTrunkClass, fieldMapper);

      graphNodeStack.push(new ReaderGraphNode(graphTrunk));
      
      JsonToken jsonToken;

      try {
         while ((jsonToken = jsonParser.nextToken()) != null) {
            if (graphNodeStack.isEmpty()) {
               throw new JxParsingException("Out of graph nodes to populate but JSON data still remains.");
            }

            final ReaderGraphNode currentGraphNode = graphNodeStack.pop();

            switch (jsonToken) {
               case FIELD_NAME:
                  final ReaderGraphNode selectedField = currentGraphNode.getObjectBuilder().getField(jsonParser.getText());

                  if (selectedField == null) {
                     LOG.warn("Encountered field that has no mapped equal. This will not be ignored. Field: " + jsonParser.getText());
                  } else {
                     // Finish with this node later.
                     graphNodeStack.push(currentGraphNode);

                     // Push the new field to process it next.
                     graphNodeStack.push(selectedField);
                  }

                  break;

               case START_OBJECT:
                  startObject(graphNodeStack, currentGraphNode);

                  break;

               case END_OBJECT:
                  endObject(currentGraphNode, graphNodeStack);

                  break;

               case START_ARRAY:

                  startArray(currentGraphNode, graphNodeStack);

                  break;

               case END_ARRAY:
                  endArray(currentGraphNode);

                  break;

               case VALUE_TRUE:
                  set(currentGraphNode, Boolean.TRUE);
                  break;

               case VALUE_FALSE:
                  set(currentGraphNode, Boolean.FALSE);
                  break;

               case VALUE_NULL:
                  set(currentGraphNode, null);
                  break;

               case VALUE_NUMBER_FLOAT:
                  set(currentGraphNode, Float.valueOf(jsonParser.getFloatValue()));
                  break;

               case VALUE_NUMBER_INT:
                  set(currentGraphNode, Integer.valueOf(jsonParser.getIntValue()));
                  break;

               case VALUE_STRING:
                  set(currentGraphNode, jsonParser.getText());
                  break;

               case VALUE_EMBEDDED_OBJECT:
                  throw new UnsupportedOperationException("Embedded object encountered. Log this as a bug with the a sample of your input, please.");

               case NOT_AVAILABLE:
                  throw new JxParsingException("NOT_AVAILABLE: Log this as a bug with the a sample of your input, please.");

            }
         }
      } catch (Exception ex) {
         if (ex instanceof JxParsingException) {
            throw (JxParsingException) ex;
         }
         
         throw new JxParsingException("Parsing JSON input failed. Reason: " + ex.getMessage(), ex);
      }

      return graphTrunk;
   }

   public void startArray(ReaderGraphNode currentGraphNode, Stack<ReaderGraphNode> graphNodeStack) throws JxParsingException {
      if (!currentGraphNode.isCollection()) {
         throw new JxParsingException("Unexpected collection start.");
      }

      graphNodeStack.push(currentGraphNode);
   }

   public void endArray(ReaderGraphNode currentGraphNode) throws JxParsingException {
      if (!currentGraphNode.isCollection()) {
         throw new JxParsingException("Unexpected collection end.");
      }
   }

   public void startObject(Stack<ReaderGraphNode> graphNodeStack, ReaderGraphNode currentGraphNode) throws ReflectionException {
      // Moving to the next field. We'll finish with this node later
      graphNodeStack.push(currentGraphNode);

      if (currentGraphNode.shouldWrap()) {
         // Object is wrapped. Ignore this start object token.
         currentGraphNode.wrapped();
      } else if (currentGraphNode.isCollection()) {
         // Moving to the next element in the collection
         final MappedCollection mappedCollection = (MappedCollection) currentGraphNode.getMappedField();
         final ReaderGraphNodeValue collectionValueBuilder = ReaderGraphNodeValue.builderFor(mappedCollection.getCollectionValueClass(), fieldMapper);

         // Push a new inspector for the collection value we're building
         graphNodeStack.push(new ReaderGraphNode(collectionValueBuilder));
      }
   }

   public void endObject(ReaderGraphNode currentGraphNode, Stack<ReaderGraphNode> graphNodeStack) throws JxParsingException {
      if (currentGraphNode.wasWrapped()) {
         // Object is wrapped. Ignore this end object token.
         graphNodeStack.push(currentGraphNode);
      } else {
         if (currentGraphNode.getObjectBuilder() == null) {
            throw new JxParsingException("Attempting to close a JSON object on a graph node that does not have an object builder. Please make sure your JSON is well formed.");
         }

         // Done with the graph node so let's get the object it built
         final Object builtObjectInstance = currentGraphNode.getObjectBuilder().getFieldOwnerInstance();

         if (currentGraphNode.hasMappedField()) {
            set(currentGraphNode, builtObjectInstance);
         } else {
            final ReaderGraphNode parentGraphNode = graphNodeStack.pop();

            if (parentGraphNode.isCollection()) {
               final ReaderGraphNode collcetionGraphNode = graphNodeStack.pop();

               if (!collcetionGraphNode.isCollection()) {
                  throw new JxParsingException("Expecting collection but the selected graph node's parent is not a collection.");
               }

               add(collcetionGraphNode, builtObjectInstance);
            } else {
               throw new JxParsingException("Unable to set built object to a field or add it to a collection. The graph node above it supports neither of these operations.");
            }
         }
      }
   }

   public void set(ReaderGraphNode fieldGraphNode, Object builtObjectInstance) throws JxParsingException {
      if (fieldGraphNode == null || !fieldGraphNode.hasMappedField()) {
         // TODO:Fix - Failure case trap
         throw new JxParsingException("TODO:Fix - Failure case trap. This is happening because of how nulls are currently outputted. No nulls, no problem.");
      }

      try {
         fieldGraphNode.getMappedField().set(builtObjectInstance);
      } catch (ReflectionException re) {
         throw new JxParsingException("Failed to set/add value on the selected graph node.", re);
      }
   }

   public void add(ReaderGraphNode collectionGraphNode, Object builtObjectInstance) throws JxParsingException {
      try {
         ((MappedCollection) collectionGraphNode.getMappedField()).add(builtObjectInstance);
      } catch (ReflectionException re) {
         throw new JxParsingException("Failed to set/add value on the selected graph node.", re);
      }
   }
}
