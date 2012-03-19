package net.jps.jx.jackson;

import java.io.IOException;
import java.util.Stack;
import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.mapping.ReaderGraphNode;
import net.jps.jx.jackson.mapping.ReaderGraphNodeValue;
import net.jps.jx.mapping.FieldMapper;
import net.jps.jx.mapping.MappedCollection;
import net.jps.jx.util.reflection.ReflectionException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public class JsonGraphReader<T> {

   private static final Logger LOG = LoggerFactory.getLogger(JsonGraphReader.class);
   
   private final Stack<ReaderGraphNode> graphNodeStack = new Stack<ReaderGraphNode>();
   private final ReaderGraphNodeValue<T> graphTrunk;
   private final FieldMapper fieldMapper;
   
   private boolean shouldSkip;
   private int skipDepth;

   public JsonGraphReader(Class<T> graphTrunkClass, FieldMapper fieldMapper) {
      this.fieldMapper = fieldMapper;
      this.graphTrunk = ReaderGraphNodeValue.builderFor(graphTrunkClass, fieldMapper);

      skipDepth = 0;
      shouldSkip = false;
   }

   public ReaderGraphNodeValue<T> render(JsonParser jsonParser) throws IOException, JxParsingException {
      graphNodeStack.push(new ReaderGraphNode(graphTrunk));

      JsonToken jsonToken;

      try {
         while ((jsonToken = jsonParser.nextToken()) != null) {
            if (graphNodeStack.isEmpty()) {
               throw new JxParsingException("Out of graph nodes to populate but JSON data still remains.");
            }

            if (shouldSkip) {
               readSkipToken(jsonToken);
            } else {
               final ReaderGraphNode currentGraphNode = graphNodeStack.pop();
               readToken(jsonParser, jsonToken, currentGraphNode);
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

   public void readToken(JsonParser jsonParser, JsonToken jsonToken, ReaderGraphNode currentGraphNode) throws IOException, JxParsingException {
      switch (jsonToken) {
         case FIELD_NAME:
            selectField(jsonParser.getText(), currentGraphNode);
            break;

         case START_OBJECT:
            startObject(currentGraphNode);
            break;

         case END_OBJECT:
            endObject(currentGraphNode);
            break;

         case START_ARRAY:
            startArray(currentGraphNode);
            break;

         case END_ARRAY:
            endArray(currentGraphNode);
            break;

         case VALUE_TRUE:
            setObject(currentGraphNode, Boolean.TRUE);
            break;

         case VALUE_FALSE:
            setObject(currentGraphNode, Boolean.FALSE);
            break;

         case VALUE_NULL:
            setObject(currentGraphNode, null);
            break;

         case VALUE_NUMBER_FLOAT:
            setNumber(currentGraphNode, Double.valueOf(jsonParser.getDoubleValue()));
            break;

         case VALUE_NUMBER_INT:
            setNumber(currentGraphNode, jsonParser.getBigIntegerValue());
            break;

         case VALUE_STRING:
            setObject(currentGraphNode, jsonParser.getText());
            break;

         case VALUE_EMBEDDED_OBJECT:
            if (!shouldSkip) {
               throw new UnsupportedOperationException("Embedded object encountered. Log this as a bug with the a sample of your input, please.");
            } else {
               updateSkip();
            }

            break;

         case NOT_AVAILABLE:
            throw new JxParsingException("NOT_AVAILABLE: Log this as a bug with the a sample of your input, please.");
      }
   }

   public void readSkipToken(JsonToken jsonToken) throws JxParsingException {
      switch (jsonToken) {
         case START_OBJECT:
         case START_ARRAY:
            skipDepth++;
            break;

         case END_OBJECT:
         case END_ARRAY:
            skipDepth--;

         case VALUE_TRUE:
         case VALUE_FALSE:
         case VALUE_NULL:
         case VALUE_NUMBER_FLOAT:
         case VALUE_NUMBER_INT:
         case VALUE_STRING:
         case VALUE_EMBEDDED_OBJECT:
            updateSkip();
            break;

         case NOT_AVAILABLE:
            throw new JxParsingException("NOT_AVAILABLE: Log this as a bug with the a sample of your input, please.");

      }
   }

   public void updateSkip() {
      shouldSkip = skipDepth > 0;
   }

   public void selectField(String fieldName, ReaderGraphNode currentGraphNode) throws JxParsingException {
      // Finish with this node later.
      graphNodeStack.push(currentGraphNode);

      final ReaderGraphNode selectedField = currentGraphNode.getObjectBuilder().getField(fieldName);

      if (selectedField == null) {
         LOG.warn("Encountered field that has no mapped equal. This will be ignored. Field: " + fieldName);
         shouldSkip = true;
      } else {
         // Push the new field to process it next.
         graphNodeStack.push(selectedField);
      }
   }

   public void startArray(ReaderGraphNode currentGraphNode) throws JxParsingException {
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

   public void startObject(ReaderGraphNode currentGraphNode) throws ReflectionException {
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

   public void endObject(ReaderGraphNode currentGraphNode) throws JxParsingException {
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
            setObject(currentGraphNode, builtObjectInstance);
         } else {
            final ReaderGraphNode collcetionGraphNode = graphNodeStack.pop();

            if (collcetionGraphNode.isCollection()) {
               add(collcetionGraphNode, builtObjectInstance);

               // Not done with the collection yet
               graphNodeStack.push(collcetionGraphNode);
            } else {
               throw new JxParsingException("Expecting collection but the selected graph node's parent is not a collection.");
            }
         }
      }
   }

   public void setNumber(ReaderGraphNode fieldGraphNode, Number number) throws JxParsingException {
      final Class fieldType = fieldGraphNode.getMappedField().getFieldType();

      if (Double.class.equals(fieldType)) {
         setObject(fieldGraphNode, Double.valueOf(number.doubleValue()));
      } else if (Float.class.equals(fieldType)) {
         setObject(fieldGraphNode, Float.valueOf(number.floatValue()));
      } else if (Long.class.equals(fieldType)) {
         setObject(fieldGraphNode, Long.valueOf(number.longValue()));
      } else if (Integer.class.equals(fieldType)) {
         setObject(fieldGraphNode, Integer.valueOf(number.intValue()));
      } else if (Short.class.equals(fieldType)) {
         setObject(fieldGraphNode, Short.valueOf(number.shortValue()));
      }
   }

   public void setObject(ReaderGraphNode fieldGraphNode, Object builtObjectInstance) throws JxParsingException {
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
