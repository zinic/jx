package net.jps.jx.jackson;

import net.jps.jx.mapping.ClassDescriptor;
import net.jps.jx.mapping.FieldDescriptor;
import net.jps.jx.mapping.ObjectConstructor;
import java.io.IOException;
import java.util.Stack;
import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.mapping.*;
import net.jps.jx.json.JsonType;
import net.jps.jx.json.JsonTypeDescriptor;
import net.jps.jx.mapping.ClassMapper;
import net.jps.jx.mapping.MappedCollectionField;
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
   private final Stack<GraphNode> readerNodeStack;
   private final Stack<CollectionFieldEntry> collectionFieldEntryStack;
   private final ClassDescriptor<T> graphTrunkClass;
   private final ObjectConstructor objectConstructor;
   private final ClassMapper classMapper;
   // TODO: replace these two suckers with an immutable aggregration class
   private boolean shouldSkip;
   private int skipDepth;

   public JsonGraphReader(ClassDescriptor<T> graphTrunkClass, ClassMapper fieldMapper, ObjectConstructor objectConstructor) {
      this.graphTrunkClass = graphTrunkClass;
      this.classMapper = fieldMapper;
      this.objectConstructor = objectConstructor;

      skipDepth = 0;
      shouldSkip = false;
      readerNodeStack = new Stack<GraphNode>();
      collectionFieldEntryStack = new Stack<CollectionFieldEntry>();
   }

   public T render(JsonParser jsonParser) throws IOException, JxParsingException {
      T graphTrunk;
      JsonToken jsonToken;

      try {
         graphTrunk = objectConstructor.newInstance(graphTrunkClass.getDescribedClass());
         readerNodeStack.push(new GraphNode(graphTrunk, graphTrunkClass));

         while ((jsonToken = jsonParser.nextToken()) != null) {
            if (readerNodeStack.isEmpty()) {
               throw new JxParsingException("Out of graph nodes to populate but JSON data still remains.");
            }

            if (shouldSkip) {
               readSkipToken(jsonToken);
            } else {
               final GraphNode currentGraphNode = readerNodeStack.pop();
               readToken(jsonParser, jsonToken, currentGraphNode);
            }
         }
      } catch (ReflectionException ex) {
         throw new JxParsingException("Parsing JSON input failed. Reason: " + ex.getMessage(), ex);
      }

      return graphTrunk;
   }

   public void readToken(JsonParser jsonParser, JsonToken jsonToken, GraphNode currentGraphNode) throws IOException, JxParsingException {
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

         case NOT_AVAILABLE:
            throw new JxParsingException("NOT_AVAILABLE: Log this as a bug with the a sample of your input, please.");

         default:
            readValueToken(jsonParser, jsonToken, currentGraphNode);
      }
   }

   public void readValueToken(JsonParser jsonParser, JsonToken jsonToken, GraphNode currentGraphNode) throws IOException, JxParsingException {
      switch (jsonToken) {
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
      }
   }

   public void readSkipToken(JsonToken jsonToken) throws JxParsingException {
      switch (jsonToken) {
         case NOT_AVAILABLE:
            throw new JxParsingException("NOT_AVAILABLE: Log this as a bug with the a sample of your input, please.");

         case START_OBJECT:
         case START_ARRAY:
            skipDepth++;
            break;

         case END_OBJECT:
         case END_ARRAY:
            skipDepth--;

         default:
            updateSkip();
            break;
      }
   }

   public void updateSkip() {
      shouldSkip = skipDepth > 0;
   }

   public void selectField(String fieldName, GraphNode currentGraphNode) throws JxParsingException {
      // Finish with this node later.
      readerNodeStack.push(currentGraphNode);

      final FieldDescriptor selectedField = currentGraphNode.getClassDescriptor().findField(fieldName);

      if (selectedField == null) {
         LOG.debug("Encountered field that has no mapped equal. This will be ignored. Field: " + fieldName);
         shouldSkip = true;
      } else {
         // Push the new field to process it next.
         readerNodeStack.push(newGraphNode(currentGraphNode, selectedField));

         // Is this a collection? Add a collection field entry then.
         if (isCollection(classMapper.describeJsonType(selectedField.getTypeDescriptor().getDescribedClass()))) {
            collectionFieldEntryStack.push(new CollectionFieldEntry(selectedField));
         }
      }
   }

   public GraphNode newGraphNode(GraphNode graphNode, FieldDescriptor field) {
      final Object presetFieldInstance = graphNode.get(field);
      final Object fieldInstance = presetFieldInstance == null ? objectConstructor.newInstance(field.getTypeDescriptor().getDescribedClass()) : presetFieldInstance;

      return new GraphNode(fieldInstance, field);
   }

   public void startArray(GraphNode currentGraphNode) throws JxParsingException {
      final Class currentNodeClass = currentGraphNode.getClassDescriptor().getDescribedClass();

      if (!isCollection(classMapper.describeJsonType(currentNodeClass))) {
         throw new JxParsingException("Unexpected collection start.");
      }

      readerNodeStack.push(currentGraphNode);
   }

   public void endArray(GraphNode currentGraphNode) throws JxParsingException {
      final Class currentNodeClass = currentGraphNode.getClassDescriptor().getDescribedClass();

      if (!isCollection(classMapper.describeJsonType(currentNodeClass))) {
         throw new JxParsingException("Unexpected collection start.");
      }

      collectionFieldEntryStack.pop();
   }

   public void startObject(GraphNode currentGraphNode) {
      final Class currentNodeClass = currentGraphNode.getClassDescriptor().getDescribedClass();

      // Moving to the next field. We'll finish with this node later
      readerNodeStack.push(currentGraphNode);

      if (isCollection(classMapper.describeJsonType(currentNodeClass))) {
         final CollectionFieldEntry collectionField = collectionFieldEntryStack.peek();

         // Moving to the next element in the collection so we'll create a new object instance.
         final MappedCollectionField mappedCollection = collectionField.getMappedCollection();
         final Object collectionValueInstance = objectConstructor.newInstance(mappedCollection.getCollectionValueClass());

         // TODO: THIS IS NOT EFFICIENT :x - cache it
         final ClassCrawler crawler = new ClassCrawler(classMapper, mappedCollection.getCollectionValueClass());

         // Push a new inspector for the collection value we're building
         readerNodeStack.push(new GraphNode(collectionValueInstance, crawler.getGraph()));
      }
   }

   public boolean isCollection(JsonTypeDescriptor jsonTypeDescriptor) {
      return jsonTypeDescriptor.getJsonType() == JsonType.ARRAY;
   }

   public void endObject(GraphNode currentGraphNode) throws JxParsingException {
      // Only set a built object if there's a place to set it - otherwise, we're at the trunk
      if (!readerNodeStack.empty()) {
         // Done with the graph node so let's get the object shoved in place.
         final Object builtObjectInstance = currentGraphNode.getInstance();

         if (currentGraphNode.isField()) {
            // Instance belongs to a field, set it.
            setObject(currentGraphNode, builtObjectInstance);
         } else {
            // Instance belongs to a collection, add it.
            add(readerNodeStack.peek(), builtObjectInstance);
         }
      }
   }

   public void setNumber(GraphNode fieldGraphNode, Number number) throws JxParsingException {
      final Class fieldType = fieldGraphNode.getClassDescriptor().getDescribedClass();

      if (double.class.equals(fieldType) || Double.class.equals(fieldType)) {
         setObject(fieldGraphNode, Double.valueOf(number.doubleValue()));
      } else if (float.class.equals(fieldType) || Float.class.equals(fieldType)) {
         setObject(fieldGraphNode, Float.valueOf(number.floatValue()));
      } else if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
         setObject(fieldGraphNode, Long.valueOf(number.longValue()));
      } else if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
         setObject(fieldGraphNode, Integer.valueOf(number.intValue()));
      } else if (short.class.equals(fieldType) || Short.class.equals(fieldType)) {
         setObject(fieldGraphNode, Short.valueOf(number.shortValue()));
      }
   }

   public void setObject(GraphNode fieldGraphNode, Object builtObjectInstance) throws JxParsingException {
      if (fieldGraphNode == null) {
         // TODO:Fix - Failure case trap
         throw new JxParsingException("TODO:Fix - Failure case trap. This is happening because of how nulls are currently outputted. No nulls, no problem.");
      }

      try {
         readerNodeStack.peek().set(fieldGraphNode.getFieldDescriptor(), builtObjectInstance);
      } catch (ReflectionException re) {
         throw new JxParsingException("Failed to set/add value on the selected graph node.", re);
      }
   }

   public void add(GraphNode collectionGraphNode, Object builtObjectInstance) throws JxParsingException {
      try {
         collectionFieldEntryStack.peek().add(collectionGraphNode.getInstance(), builtObjectInstance);
      } catch (ReflectionException re) {
         throw new JxParsingException("Failed to set/add value on the selected graph node.", re);
      }
   }
}
