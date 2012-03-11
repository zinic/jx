package net.jps.jx.jackson.mapping;

/**
 *
 * @author zinic
 */
public class ObjectGraphNode {

   private final ObjectGraphBuilder objectBuilder;
   private final MappedField mappedField;

   public ObjectGraphNode(ObjectGraphBuilder objectBuilder, MappedField mappedField) {
      this.objectBuilder = objectBuilder;
      this.mappedField = mappedField;
   }

   public MappedField getMappedField() {
      return mappedField;
   }

   public ObjectGraphBuilder getObjectBuilder() {
      return objectBuilder;
   }
}
