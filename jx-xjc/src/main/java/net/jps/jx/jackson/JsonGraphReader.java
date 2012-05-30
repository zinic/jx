package net.jps.jx.jackson;

import java.io.IOException;
import net.jps.jx.mapping.ClassDescriptor;
import net.jps.jx.mapping.ObjectConstructor;
import java.util.Stack;
import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.plugin.*;
import net.jps.jx.jackson.plugin.stock.DefaultJsonGraphMapper;
import net.jps.jx.jackson.plugin.impl.fieldmapping.SequenceRemappingMapper;
import net.jps.jx.jackson.plugin.operation.RenderAction;
import net.jps.jx.jackson.plugin.operation.RenderOperation;
import net.jps.jx.mapping.ClassMapper;
import net.jps.jx.mapping.FieldDescriptor;
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
   
   private final JsonGraphEventHandler[] graphEventHandlers;
   private final ClassDescriptor<T> graphTrunkClass;
   private final GraphContext graphContext;
   private final SkipTracker skipTracker;
   
   public JsonGraphReader(ClassDescriptor<T> graphTrunkClass, ClassMapper classMapper, ObjectConstructor objectConstructor) {
      this.graphTrunkClass = graphTrunkClass;
      
      skipTracker = new SkipTracker();
      graphContext = new GraphContextImpl(new Stack<CollectionFieldEntry>(), new Stack<GraphNode>(), objectConstructor, classMapper, skipTracker);
      graphEventHandlers = new JsonGraphEventHandler[]{new SequenceRemappingMapper(graphContext), new DefaultJsonGraphMapper(graphContext)};
   }
   
   public T render(JsonParser jsonParser) throws IOException, JxParsingException {
      T graphTrunk = null;
      
      try {
         graphTrunk = graphContext.newInstance(graphTrunkClass.getDescribedClass());
         graphContext.pushGraphNode(new GraphNode(graphTrunk, graphTrunkClass));
         
         JsonToken jsonToken;
         
         while ((jsonToken = jsonParser.nextToken()) != null) {
            if (!graphContext.hasMoreGraphNodes()) {
               throw new JxParsingException("Out of graph nodes to populate but JSON data still remains.");
            }
            
            if (skipTracker.shouldSkip()) {
               skipToken(jsonToken);
            } else {
               renderToken(jsonParser, jsonToken);
            }
         }
      } catch (ReflectionException ex) {
         throw new JxParsingException("Rendering JSON object graph failed. Reason: " + ex.getMessage(), ex);
      }
      
      return graphTrunk;
   }
   
   private void renderToken(JsonParser jsonParser, JsonToken jsonToken) throws IOException, JxParsingException {
      // Run the token through the render chain
      for (JsonGraphEventHandler graphEventHandler : graphEventHandlers) {
         final GraphNode currentGraphNode = graphContext.popGraphNode();
         final RenderResult renderResult = renderNode(jsonParser, jsonToken, currentGraphNode, graphEventHandler);
         
         if (renderResult.hasRenderActions()) {
            processRenderActions(renderResult);
         }
         
         if (renderResult.chainAction() == RenderChainAction.EXIT) {
            // Exit the render chain
            break;
         }
      }
   }
   
   private void processRenderActions(RenderResult renderResult) {
      for (RenderAction action : renderResult.renderActions()) {
         switch (action.operation()) {
            case POP:
               graphContext.popGraphNode();
               break;
            
            case POP_COLLECTION:
               graphContext.popCollection();
               break;
            
            case PUSH:
               graphContext.pushGraphNode((GraphNode) action.payload());
               break;
            
            case PUSH_COLLECTION:
               graphContext.pushCollection((CollectionFieldEntry) action.payload());
               break;
         }
      }
   }
   
   private RenderResult renderNode(JsonParser jsonParser, JsonToken jsonToken, GraphNode currentGraphNode, JsonGraphEventHandler graphEventHandler) throws IOException, JxParsingException {
      switch (jsonToken) {
         case FIELD_NAME:
            return graphEventHandler.selectField(currentGraphNode, jsonParser.getText());
         
         case START_OBJECT:
            return graphEventHandler.startObject(currentGraphNode);
         
         case END_OBJECT:
            return graphEventHandler.endObject(currentGraphNode);
         
         case START_ARRAY:
            return graphEventHandler.startArray(currentGraphNode);
         
         case END_ARRAY:
            return graphEventHandler.endArray(currentGraphNode);
         
         case NOT_AVAILABLE:
            throw new JxParsingException("NOT_AVAILABLE: Log this as a bug with the a sample of your input, please.");
         
         default:
            return renderValue(jsonParser, jsonToken, currentGraphNode, graphEventHandler);
      }
   }
   
   private RenderResult renderValue(JsonParser jsonParser, JsonToken jsonToken, GraphNode currentGraphNode, JsonGraphEventHandler graphEventHandler) throws IOException, JxParsingException {
      switch (jsonToken) {
         case VALUE_TRUE:
            return graphEventHandler.setObject(currentGraphNode, Boolean.TRUE);
         
         case VALUE_FALSE:
            return graphEventHandler.setObject(currentGraphNode, Boolean.FALSE);
         
         case VALUE_NULL:
            return graphEventHandler.setObject(currentGraphNode, null);
         
         case VALUE_NUMBER_FLOAT:
            return graphEventHandler.setNumber(currentGraphNode, Double.valueOf(jsonParser.getDoubleValue()));
         
         case VALUE_NUMBER_INT:
            return graphEventHandler.setNumber(currentGraphNode, jsonParser.getBigIntegerValue());
         
         case VALUE_STRING:
            return graphEventHandler.setObject(currentGraphNode, jsonParser.getText());
         
         case VALUE_EMBEDDED_OBJECT:
            if (!skipTracker.shouldSkip()) {
               throw new UnsupportedOperationException("Embedded object encountered. Log this as a bug with the a sample of your input, please.");
            }
         
         default:
            return Results.exitRenderChain();
      }
   }
   
   private void skipToken(JsonToken jsonToken) throws JxParsingException {
      switch (jsonToken) {
         case NOT_AVAILABLE:
            throw new JxParsingException("NOT_AVAILABLE: Log this as a bug with the a sample of your input, please.");
         
         case START_OBJECT:
         case START_ARRAY:
            skipTracker.descend();
            break;
         
         case END_OBJECT:
         case END_ARRAY:
            skipTracker.ascend();
            break;
      }
   }
}
