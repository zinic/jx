package net.jps.jx.jackson.plugin.stock;

import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.CollectionFieldEntry;
import net.jps.jx.jackson.GraphNode;
import net.jps.jx.jackson.mapping.ClassCrawler;
import net.jps.jx.jackson.plugin.*;
import net.jps.jx.jackson.plugin.operation.RenderAction;
import net.jps.jx.jackson.plugin.operation.RenderOperation;
import net.jps.jx.json.JsonType;
import net.jps.jx.mapping.FieldDescriptor;
import net.jps.jx.mapping.MappedCollectionField;
import net.jps.jx.util.reflection.ReflectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public class DefaultJsonGraphMapper extends PassiveEventHandler {

   private static final Logger LOG = LoggerFactory.getLogger(DefaultJsonGraphMapper.class);
   private final GraphContext graphContext;

   public DefaultJsonGraphMapper(GraphContext graphContext) {
      this.graphContext = graphContext;
   }

   @Override
   public RenderResult selectField(GraphNode currentGraphNode, String fieldName) throws JxParsingException {
      final FieldDescriptor selectedField = currentGraphNode.getClassDescriptor().findField(fieldName);
      final RenderResultImpl selectFieldResult = new RenderResultImpl(RenderChainAction.EXIT);

      // Push the current graph node, we're not done with it
      selectFieldResult.addAction(new RenderAction(RenderOperation.PUSH, currentGraphNode));

      if (selectedField != null) {
         // Push the new field to process it next.
         selectFieldResult.addAction(new RenderAction(RenderOperation.PUSH, newGraphNode(graphContext, currentGraphNode, selectedField)));

         // Is this a collection? Add a collection field entry then.
         if (JsonType.isCollection(graphContext.classMapper().describeJsonType(selectedField.getTypeDescriptor().getDescribedClass()))) {
            selectFieldResult.addAction(new RenderAction(RenderOperation.PUSH_COLLECTION, new CollectionFieldEntry(selectedField)));
         }
      } else {
         LOG.debug("Encountered field that has no mapped equal. This will be ignored. Field: " + fieldName);
         graphContext.skipOnce();
      }

      return selectFieldResult;
   }

   @Override
   public RenderResult startArray(GraphNode currentGraphNode) throws JxParsingException {
      final Class currentNodeClass = currentGraphNode.getClassDescriptor().getDescribedClass();

      if (!JsonType.isCollection(graphContext.classMapper().describeJsonType(currentNodeClass))) {
         throw new JxParsingException("Unexpected collection start.");
      }

      return Results.pushThenExit(currentGraphNode);
   }

   @Override
   public RenderResult endArray(GraphNode currentGraphNode) throws JxParsingException {
      final Class currentNodeClass = currentGraphNode.getClassDescriptor().getDescribedClass();

      if (!JsonType.isCollection(graphContext.classMapper().describeJsonType(currentNodeClass))) {
         throw new JxParsingException("Unexpected collection start.");
      }

      graphContext.popCollection();
      return Results.exitRenderChain();
   }

   @Override
   public RenderResult startObject(GraphNode currentGraphNode) {
      final Class currentNodeClass = currentGraphNode.getClassDescriptor().getDescribedClass();

      // Moving to the next field. We'll finish with this node later
      graphContext.pushGraphNode(currentGraphNode);

      if (JsonType.isCollection(graphContext.classMapper().describeJsonType(currentNodeClass))) {
         final CollectionFieldEntry collectionField = graphContext.peekCollection();

         // Moving to the next element in the collection so we'll create a new object instance.
         final MappedCollectionField mappedCollection = collectionField.getMappedCollection();
         final Object collectionValueInstance = graphContext.newInstance(mappedCollection.getCollectionValueClass());

         // TODO: THIS IS NOT EFFICIENT :x - cache it
         final ClassCrawler crawler = new ClassCrawler(graphContext.classMapper(), mappedCollection.getCollectionValueClass());

         // Push a new inspector for the collection value we're building
         graphContext.pushGraphNode(new GraphNode(collectionValueInstance, crawler.getGraph()));
      }

      return Results.exitRenderChain();
   }

   @Override
   public RenderResult endObject(GraphNode currentGraphNode) throws JxParsingException {
      // Only set a built object if there's a place to set it - otherwise, we're at the trunk
      if (graphContext.hasMoreGraphNodes()) {
         // Done with the graph node so let's get the object shoved in place.
         final Object builtObjectInstance = currentGraphNode.getInstance();

         if (currentGraphNode.isField()) {
            // Instance belongs to a field, set it.
            setObject(currentGraphNode, builtObjectInstance);
         } else {
            // Instance belongs to a collection, add it.
            add(graphContext.peekGraphNode(), builtObjectInstance);
         }
      }

      return Results.exitRenderChain();
   }

   @Override
   public RenderResult setNumber(GraphNode fieldGraphNode, Number number) throws JxParsingException {
      final Class fieldType = fieldGraphNode.getClassDescriptor().getDescribedClass();

      if (double.class.equals(fieldType) || Double.class.equals(fieldType)) {
         return setObject(fieldGraphNode, Double.valueOf(number.doubleValue()));
      } else if (float.class.equals(fieldType) || Float.class.equals(fieldType)) {
         return setObject(fieldGraphNode, Float.valueOf(number.floatValue()));
      } else if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
         return setObject(fieldGraphNode, Long.valueOf(number.longValue()));
      } else if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
         return setObject(fieldGraphNode, Integer.valueOf(number.intValue()));
      } else if (short.class.equals(fieldType) || Short.class.equals(fieldType)) {
         return setObject(fieldGraphNode, Short.valueOf(number.shortValue()));
      }

      throw new JxParsingException("Unknown Number class: " + number.getClass().getCanonicalName());
   }

   @Override
   public RenderResult setObject(GraphNode fieldGraphNode, Object builtObjectInstance) throws JxParsingException {
      if (fieldGraphNode == null) {
         // TODO:Fix - Failure case trap
         throw new JxParsingException("TODO:Fix - Failure case trap. This is happening because of how nulls are currently outputted. No nulls, no problem.");
      }

      try {
         graphContext.peekGraphNode().set(fieldGraphNode.getFieldDescriptor(), builtObjectInstance);
      } catch (ReflectionException re) {
         throw new JxParsingException("Failed to set/add value on the selected graph node. Reason: " + re.getCause().getMessage(), re);
      }

      return Results.exitRenderChain();
   }

   private void add(GraphNode collectionGraphNode, Object builtObjectInstance) throws JxParsingException {
      try {
         graphContext.peekCollection().add(collectionGraphNode.getInstance(), builtObjectInstance);
      } catch (ReflectionException re) {
         throw new JxParsingException("Failed to set/add value on the selected graph node.", re);
      }
   }
}
