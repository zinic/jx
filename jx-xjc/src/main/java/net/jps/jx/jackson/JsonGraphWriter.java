package net.jps.jx.jackson;

import net.jps.jx.mapping.ClassMapper;
import net.jps.jx.mapping.JsonNumberWriter;
import net.jps.jx.json.JsonTypeDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;
import net.jps.jx.JxWritingException;
import net.jps.jx.jackson.mapping.ClassCrawler;
import net.jps.jx.jackson.mapping.ClassDescriptor;
import net.jps.jx.jackson.mapping.FieldDescriptor;
import net.jps.jx.jackson.mapping.JacksonNumberWriter;
import net.jps.jx.util.reflection.ReflectionException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author zinic
 */
public class JsonGraphWriter {

    private final Stack<WriterGraphNode> graphNodeStack;
    private final Stack<Iterator> iteratorStack;
    private final JsonFactory jsonFactory;
    private final ClassMapper classMapper;

    public JsonGraphWriter(JsonFactory jsonFactory, ClassMapper classMapper) {
        graphNodeStack = new Stack<WriterGraphNode>();
        iteratorStack = new Stack<Iterator>();

        this.jsonFactory = jsonFactory;
        this.classMapper = classMapper;
    }

    public void write(Object rootObject, OutputStream outputStream) throws IOException, JxWritingException {
        final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream);
        final JsonNumberWriter jsonNumberWriter = new JacksonNumberWriter(jsonGenerator);

        // Root node
        // TODO: THIS IS NOT EFFICIENT :x - cache it
        final ClassCrawler rootClassCrawler = new ClassCrawler(classMapper, rootObject.getClass());
        write(jsonGenerator, jsonNumberWriter, rootClassCrawler.getGraph(), rootObject);

        try {
            while (!graphNodeStack.isEmpty()) {
                final WriterGraphNode currentGraphNode = graphNodeStack.pop();

                if (!classMapper.isCollection(currentGraphNode.getClassDescriptor().getDescribedClass())) {
                    // Not a collection, inspect this object

                    if (currentGraphNode.hasNextField()) {
                        // This node has remaining fields to inspect, deal with it later. Push it
                        graphNodeStack.push(currentGraphNode);

                        // Get the node's next field
                        final FieldDescriptor nextField = currentGraphNode.nextField();
                        final Object fieldInstance = currentGraphNode.get(nextField);

                        // Ignore null
                        if (fieldInstance != null) {
                            jsonGenerator.writeFieldName(nextField.getName());
                            write(jsonGenerator, jsonNumberWriter, nextField.getTypeDescriptor(), fieldInstance);
                        }
                    } else {
                        // Done with this object
                        jsonGenerator.writeEndObject();
                    }
                } else {
                    // A collection is being processed
                    final Iterator collectionIterator = iteratorStack.pop();

                    if (collectionIterator.hasNext()) {
                        // Not done with the collection yet. Push it back onto the stack.
                        graphNodeStack.push(currentGraphNode);
                        iteratorStack.push(collectionIterator);

                        final Object collectionValue = collectionIterator.next();
                        
                        if (collectionValue != null) {
                            // TODO: THIS IS NOT EFFICIENT :x - cache it
                            final ClassCrawler classCrawler = new ClassCrawler(classMapper, collectionValue.getClass());

                            // Process the object
                            write(jsonGenerator, jsonNumberWriter, classCrawler.getGraph(), collectionValue);
                        }
                    } else {
                        // Finished with this array
                        jsonGenerator.writeEndArray();
                    }
                }
            }
        } catch (JxWritingException jwe) {
//         unwindObjectGraphStack(jwe, graphNodeStack);

            throw jwe;
        } catch (ReflectionException ex) {
//         unwindObjectGraphStack(ex, graphNodeStack);

            throw new JxWritingException("Failed to write object graph to output stream as JSON. Reason: " + ex.getMessage(), ex);
        } finally {
            if (jsonGenerator != null) {
                jsonGenerator.close();
            }
        }
    }

//   private void unwindObjectGraphStack(Exception ex, Stack<WriterGraphNode> graphNodeStack) {
//      if (LOG.isDebugEnabled()) {
//         final StringBuilder outputBuilder = new StringBuilder("Exception caught during writing. Failure reason: ");
//         outputBuilder.append(ex.getMessage()).append("\nObject Graph Stack Dump");
//
//         while (!graphNodeStack.isEmpty()) {
//            final WriterGraphNode wgn = graphNodeStack.pop();
//
//            outputBuilder.append("Object Graph Node: object:").append(wgn.getValueObject().toString()).append(" - class: ");
//            outputBuilder.append(wgn.getValueObject().getClass().getName()).append(" - toString: ").append(wgn.getValueObject().toString());
//         }
//
//         LOG.error(outputBuilder.toString(), ex);
//      }
//   }
    
    private void write(JsonGenerator jsonGenerator, JsonNumberWriter jsonNumberWriter, ClassDescriptor classDescriptor, Object valueBeingWritten) throws IOException, JxWritingException {
        // Check the value of the next field
        final JsonTypeDescriptor jtd = classMapper.describeJsonType(valueBeingWritten.getClass());

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
                classMapper.writeNumber(valueBeingWritten, jsonNumberWriter);
                break;

            case ARRAY:
                startArray(jsonGenerator, classDescriptor, valueBeingWritten);
                break;

            case OBJECT:
                startObject(jsonGenerator, classDescriptor, valueBeingWritten);
                break;
        }
    }

    private void startArray(JsonGenerator jsonGenerator, ClassDescriptor classDescriptor, Object valueBeingWritten) throws IOException {
        // New iterable object
        jsonGenerator.writeStartArray();

        // Push a new inspector node for the collection
        graphNodeStack.push(new WriterGraphNode(valueBeingWritten, classDescriptor));

        // Add the collection's iterator to the iterator stack
        iteratorStack.push(((Iterable) valueBeingWritten).iterator());
    }

    private void startObject(JsonGenerator jsonGenerator, ClassDescriptor classDescriptor, Object valueBeingWritten) throws IOException {
        // New object
        jsonGenerator.writeStartObject();

        // Push a new inspector node for the object
        graphNodeStack.push(new WriterGraphNode(valueBeingWritten, classDescriptor));
    }
}
