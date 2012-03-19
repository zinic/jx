package net.jps.jx.jackson;

import net.jps.jx.mapping.FieldMapper;
import net.jps.jx.mapping.JsonNumberWriter;
import net.jps.jx.mapping.MappedField;
import net.jps.jx.json.JsonTypeDescriptor;
import net.jps.jx.jackson.mapping.WriterGraphNode;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;
import net.jps.jx.JsonWriter;
import net.jps.jx.JxWritingException;
import net.jps.jx.jackson.mapping.JacksonNumberWriter;
import net.jps.jx.util.reflection.ReflectionException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Playing with my own version of auto-serializing JSON writer for JAXB
 * annotated entities
 *
 * This is just an experiment with Jackson at a lower API in order to acquire
 * more fine-grained control of the JSON that is produced during the
 * serialization process.
 *
 * This is far from complete and has no tests.
 *
 * @author zinic
 */
public class JacksonJsonWriter<T> implements JsonWriter<T> {

   private static final Logger LOG = LoggerFactory.getLogger(JacksonJsonWriter.class);
   
   public static volatile boolean ignoreNulls = true;
   
   private final JsonFactory jsonFactory;
   private final FieldMapper fieldMapper;

   public JacksonJsonWriter(JsonFactory jsonFactory, FieldMapper fieldMapper) {
      this.jsonFactory = jsonFactory;
      this.fieldMapper = fieldMapper;
   }

   @Override
   public void write(T rootObject, OutputStream outputStream) throws IOException, JxWritingException {
      final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream);
      final JsonNumberWriter jsonNumberWriter = new JacksonNumberWriter(jsonGenerator);
      final Stack<WriterGraphNode> graphNodeStack = new Stack<WriterGraphNode>();
      final Stack<Iterator> iteratorStack = new Stack<Iterator>();

      // Root node
      processObject(rootObject, jsonGenerator, jsonNumberWriter, graphNodeStack, iteratorStack);

      try {
         while (!graphNodeStack.isEmpty()) {
            final WriterGraphNode currentGraphNode = graphNodeStack.pop();

            if (!currentGraphNode.isIterable()) {
               // Not a collection, inspect this object

               if (currentGraphNode.hasNextField()) {
                  // This node has remaining fields to inspect, deal with it later. Push it
                  graphNodeStack.push(currentGraphNode);

                  // Get the node's next field
                  final MappedField nextField = currentGraphNode.nextMappedField();

                  // Process the object
                  final Object nextFieldValue = nextField.get();

                  if (nextFieldValue != null || !ignoreNulls) {
                     jsonGenerator.writeFieldName(nextField.getName());
                     processObject(nextField.get(), jsonGenerator, jsonNumberWriter, graphNodeStack, iteratorStack);
                  }
               } else {
                  // Done with this object
                  jsonGenerator.writeEndObject();
               }
            } else {
               // A collection is being processed
               final Iterator collectionIterator = iteratorStack.pop();

               if (collectionIterator.hasNext()) {
                  final Object collectionValue = collectionIterator.next();

                  // Not done with the collection yet. Push it back onto the stack.
                  graphNodeStack.push(currentGraphNode);
                  iteratorStack.push(collectionIterator);

                  // Process the object
                  processObject(collectionValue, jsonGenerator, jsonNumberWriter, graphNodeStack, iteratorStack);
               } else {
                  // Finished with this array
                  jsonGenerator.writeEndArray();
               }
            }
         }
      } catch (Exception ex) {
         if (LOG.isDebugEnabled()) {
         final StringBuilder outputBuilder = new StringBuilder("Exception caught during writing. Failure reason: ");
         outputBuilder.append(ex.getMessage()).append("\nObject Graph Stack Dump");

         while (!graphNodeStack.isEmpty()) {
            final WriterGraphNode wgn = graphNodeStack.pop();

            outputBuilder.append("Object Graph Node: object:").append(wgn.getValueObject().toString()).append(" - class: ");
            outputBuilder.append(wgn.getValueObject().getClass().getName()).append(" - toString: ").append(wgn.getValueObject().toString());
         }

         LOG.error(outputBuilder.toString(), ex);
         }
         
         if (ex instanceof JxWritingException) {
            throw (JxWritingException) ex;
         }
         
         throw new JxWritingException("Failed to write object graph to output stream as JSON. Reason: " + ex.getMessage(), ex);
      } finally {
         if (jsonGenerator != null) {
            jsonGenerator.close();
         }
      }
   }

   private WriterGraphNode processObject(Object valueBeingWritten, JsonGenerator jsonGenerator, JsonNumberWriter jsonNumberWriter, Stack<WriterGraphNode> graphNodeStack, Stack<Iterator> iteratorStack) throws ReflectionException, IOException, JxWritingException {
      // Check the value of the next field
      final JsonTypeDescriptor jtd = fieldMapper.getJsonType(valueBeingWritten);
      WriterGraphNode newGraphNode = null;

      // TODO:Review - The JsonType helps direct marshalling of Java classes? Duno... kinda dig it.
      switch (jtd.getJsonType()) {
         case NULL:
            jsonGenerator.writeNull();
            break;

         case STRING:
            jsonGenerator.writeString(valueBeingWritten.toString());
            break;

         case BOOLEAN:
            jsonGenerator.writeBoolean((Boolean) valueBeingWritten);
            break;

         case NUMBER:
            fieldMapper.writeNumber(valueBeingWritten, jsonNumberWriter);
            break;

         case ARRAY:
            // New iterable object
            jsonGenerator.writeStartArray();

            // Push a new inspector node for the collection
            newGraphNode = new WriterGraphNode(valueBeingWritten, fieldMapper);
            graphNodeStack.push(newGraphNode);

            // Add the collection's iterator to the iterator stack
            iteratorStack.push(((Iterable) valueBeingWritten).iterator());
            break;

         case OBJECT:
            // New object
            jsonGenerator.writeStartObject();

            // Push a new inspector node for the object
            newGraphNode = new WriterGraphNode(valueBeingWritten, fieldMapper);
            graphNodeStack.push(newGraphNode);
            break;
      }

      return newGraphNode;
   }
}
