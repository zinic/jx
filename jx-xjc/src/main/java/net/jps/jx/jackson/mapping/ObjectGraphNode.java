package net.jps.jx.jackson.mapping;

import net.jps.jx.util.reflection.JxAnnotationTool;
import net.jps.jx.util.reflection.MappedCollection;

/**
 *
 * @author zinic
 */
public class ObjectGraphNode {

   private final ObjectGraphBuilder objectBuilder;
   private final MappedField mappedField;
   private boolean wrapped;

   public ObjectGraphNode(ObjectGraphBuilder objectBuilder, MappedField mappedField) {
      this.objectBuilder = objectBuilder;
      this.mappedField = mappedField;

      wrapped = false;
   }

   public boolean isCollection() {
      return mappedField != null && mappedField.isCollection();
   }

   public MappedField getMappedField() {
      return mappedField;
   }

   public MappedCollection getMappedCollection() {
      return (MappedCollection) mappedField;
   }

   public ObjectGraphBuilder getObjectBuilder() {
      return objectBuilder;
   }

   public boolean shouldWrap() {
      return JxAnnotationTool.shouldWrapClass(objectBuilder.getInstanceClass()) && !wrapped;
   }

   public boolean isWrapped() {
      return wrapped;
   }

   public void setWrapping(boolean wrapped) {
      this.wrapped = wrapped;
   }
   
   @Override
   public String toString() {
      return "ObjectGraphNode: " + objectBuilder.getInstanceClass().getName() + " - Bound to field: " + (mappedField != null ? mappedField.getName() : "null");
   }
}
