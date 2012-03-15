package net.jps.jx.jackson.mapping;

import net.jps.jx.mapping.MappedCollection;
import net.jps.jx.mapping.MappedEnumeration;
import net.jps.jx.mapping.MappedField;
import net.jps.jx.util.reflection.JxAnnotationTool;

/**
 *
 * @author zinic
 */
public class ReaderGraphNode {

   private final ReaderGraphNodeValue objectBuilder;
   private final boolean fieldHasWrapAnnotation;
   private final MappedField mappedField;
   private Boolean wrapped;

   public ReaderGraphNode(ReaderGraphNodeValue objectGraphBuilder) {
      this(objectGraphBuilder, null);
   }

   public ReaderGraphNode(ReaderGraphNodeValue objectBuilder, MappedField mappedField) {
      this.mappedField = mappedField;
      this.objectBuilder = objectBuilder;

      fieldHasWrapAnnotation = objectBuilder != null && JxAnnotationTool.shouldWrapClass(objectBuilder.getFieldOwnerInstance().getClass());
      
      wrapped = false;
   }

   public MappedField getMappedField() {
      return mappedField;
   }

   public boolean hasMappedField() {
      return mappedField != null;
   }

   public boolean isCollection() {
      return mappedField != null && MappedCollection.class.isAssignableFrom(mappedField.getClass());
   }

   public boolean isEnumeration() {
      return mappedField != null && MappedEnumeration.class.isAssignableFrom(mappedField.getClass());
   }

   public ReaderGraphNodeValue getObjectBuilder() {
      return objectBuilder;
   }

   public boolean shouldWrap() {
      return fieldHasWrapAnnotation && !wrapped;
   }

   public boolean wasWrapped() {
      return fieldHasWrapAnnotation && wrapped;
   }

   public void wrapped() {
      this.wrapped = Boolean.TRUE;
   }
}
