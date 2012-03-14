package net.jps.jx.jackson.mapping;

import net.jps.jx.util.reflection.JxAnnotationTool;

/**
 *
 * @author zinic
 */
public class ReaderGraphNode extends AbstractGraphNode {

   private final ObjectGraphBuilder objectBuilder;
   private boolean wrapped;

   public ReaderGraphNode(ObjectGraphBuilder objectBuilder, MappedField mappedField) {
      super(mappedField);

      this.objectBuilder = objectBuilder;
      wrapped = false;
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
      return "ObjectGraphNode: " + objectBuilder.getInstanceClass().getName() + " - Bound to field: " + (getMappedField() != null ? getMappedField().getName() : "null");
   }
}
