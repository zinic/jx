package net.jps.jx.jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;
import net.jps.jx.JsonWriter;
import net.jps.jx.JxWritingException;
import net.jps.jx.jackson.mapping.*;
import net.jps.jx.util.reflection.ReflectionException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

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

   public static boolean IGNORE_NULLS = true;
   
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
                  
                  if (nextFieldValue != null || !IGNORE_NULLS) {
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
         System.out.println("Exception caught during writing. Failure reason: " + ex.getMessage());
         System.out.println("Object Graph Stack Dump");


         while (!graphNodeStack.isEmpty()) {
            final WriterGraphNode wgn = graphNodeStack.pop();

            System.out.println("Object Graph Node: object:" + wgn.getValueObject().toString() + " - class: " + wgn.getValueObject().getClass().getName() + " - toString: " + wgn.getValueObject().toString());
         }

         ex.printStackTrace(System.out);
      }

      jsonGenerator.close();
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
