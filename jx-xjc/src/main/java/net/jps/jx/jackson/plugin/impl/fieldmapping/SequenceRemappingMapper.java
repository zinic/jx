package net.jps.jx.jackson.plugin.impl.fieldmapping;

import net.jps.jx.JxParsingException;
import net.jps.jx.annotation.SequenceObjectMapping;
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
 * Confusing name, eh?
 *
 * @author zinic
 */
public class SequenceRemappingMapper extends PassiveEventHandler {
   
   private static enum Attributes {
      
      SEQUENCE_INSTANCE_BINDING
   }
   private static final Logger LOG = LoggerFactory.getLogger(SequenceRemappingMapper.class);
   private final GraphContext graphContext;
   
   public SequenceRemappingMapper(GraphContext graphContext) {
      this.graphContext = graphContext;
   }
   
   @Override
   public RenderResult selectField(GraphNode currentGraphNode, String fieldName) throws JxParsingException {
      final SequenceObjectMapping mapping = (SequenceObjectMapping) currentGraphNode.getAttribute(Attributes.SEQUENCE_INSTANCE_BINDING);
      
      if (mapping != null) {
         final RenderResultImpl selectFieldResult = new RenderResultImpl(RenderChainAction.EXIT);
         final CollectionFieldEntry collectionField = graphContext.peekCollection();

         // Moving to the next element in the collection so we'll create a new object instance.
         final MappedCollectionField mappedCollection = collectionField.getMappedCollection();
         final Object collectionValueInstance = graphContext.newInstance(mappedCollection.getCollectionValueClass());

         // TODO: THIS IS NOT EFFICIENT :x - cache it
         final ClassCrawler crawler = new ClassCrawler(graphContext.classMapper(), mappedCollection.getCollectionValueClass());

         // Push a new inspector for the collection value we're building
         final GraphNode collectionValueNode = new GraphNode(collectionValueInstance, crawler.getGraph());

         // If there's a mapping and the selected field name matched what the mapping expects we have to use special logic
         final FieldDescriptor jsonNameKeyField = collectionValueNode.getClassDescriptor().findField(mapping.fieldNameTarget());
         
         if (jsonNameKeyField != null) {
            // Set the key value first
            try {
               collectionValueNode.set(jsonNameKeyField, fieldName);
            } catch (ReflectionException re) {
               throw new JxParsingException("Failed to set/add value on the selected graph node.", re);
            }

            // Push the actual field to process it next
            final FieldDescriptor mappingTarget = SequenceObjectMapping.DEFAULT_VALUE_TARGET.equals(mapping.valueTarget())
                    ? collectionValueNode.getClassDescriptor().findField("value")
                    : collectionValueNode.getClassDescriptor().findField(mapping.valueTarget());
            
            if (mappingTarget == null) {
               throw new JxParsingException("Mapping target for sequence value, \"valueTarget\" does not map to a field in class: " + collectionValueNode.getClassDescriptor().getDescribedClass().getCanonicalName());
            }

            // Push the current graph node, it represents the collection and we're not done with it yet
            selectFieldResult.addAction(new RenderAction(RenderOperation.PUSH, currentGraphNode));

            // Push the new collection value instance
            selectFieldResult.addAction(new RenderAction(RenderOperation.PUSH, collectionValueNode));

            // Push the selected, aka the value field
            selectFieldResult.addAction(new RenderAction(RenderOperation.PUSH, newGraphNode(graphContext, collectionValueNode, mappingTarget)));

            // Is this a collection? Add a collection field entry then.
            if (JsonType.isCollection(graphContext.classMapper().describeJsonType(mappingTarget.getTypeDescriptor().getDescribedClass()))) {
               selectFieldResult.addAction(new RenderAction(RenderOperation.PUSH_COLLECTION, new CollectionFieldEntry(mappingTarget)));
            }
            
            return selectFieldResult;
         } else {
            LOG.debug("Encountered field that has no mapped equal. This will be ignored. Field: " + fieldName);
            graphContext.skipOnce();
         }
      }
      
      return Results.deferNode(currentGraphNode);
   }
   
   @Override
   public RenderResult startObject(GraphNode currentGraphNode) {
      final Class currentNodeClass = currentGraphNode.getClassDescriptor().getDescribedClass();
      
      if (JsonType.isCollection(graphContext.classMapper().describeJsonType(currentNodeClass))) {
         final SequenceObjectMapping mappingAnnotation = (SequenceObjectMapping) currentGraphNode.getFieldDescriptor().getMappedField().getField().getAnnotation(SequenceObjectMapping.class);
         
         if (mappingAnnotation != null) {
            currentGraphNode.putAttribute(Attributes.SEQUENCE_INSTANCE_BINDING, mappingAnnotation);

            // Not quite done with the current node, push it
            final RenderResultImpl result = new RenderResultImpl(RenderChainAction.EXIT);
            result.addAction(new RenderAction(RenderOperation.PUSH, currentGraphNode));
            
            return result;
         }
      }
      
      return Results.deferNode(currentGraphNode);
   }
}
