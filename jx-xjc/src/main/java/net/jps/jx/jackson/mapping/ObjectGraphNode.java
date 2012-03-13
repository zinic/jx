package net.jps.jx.jackson.mapping;

import net.jps.jx.util.reflection.JxAnnotationTool;

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
      return mappedField != null && MappedCollection.class.isAssignableFrom(mappedField.getClass());
   }

   public boolean isEnumeration() {
      return mappedField != null && MappedEnumeration.class.isAssignableFrom(mappedField.getClass());
   }

   public MappedField getMappedField() {
      return mappedField;
   }

   public ObjectGraphBuilder getObjectBuilder() {
      return objectBuilder;
   }

   public boolean shouldWrap() {
      return objectBuilder != null && JxAnnotationTool.shouldWrapClass(objectBuilder.getInstanceClass()) && !wrapped;
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
