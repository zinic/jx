package net.jps.jx.jackson.mapping;

/**
 *
 * @author zinic
 */
public abstract class AbstractGraphNode {

   private final MappedField mappedField;

   public AbstractGraphNode(MappedField mappedField) {
      this.mappedField = mappedField;
   }

   public MappedField getMappedField() {
      return mappedField;
   }

   public boolean isCollection() {
      return mappedField != null && MappedCollection.class.isAssignableFrom(mappedField.getClass());
   }

   public boolean isEnumeration() {
      return mappedField != null && MappedEnumeration.class.isAssignableFrom(mappedField.getClass());
   }
}
