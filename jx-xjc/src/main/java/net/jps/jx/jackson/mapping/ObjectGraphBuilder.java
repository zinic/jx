package net.jps.jx.jackson.mapping;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.jps.jx.util.reflection.*;

/**
 *
 * @author zinic
 */
public class ObjectGraphBuilder<T> {
   
   public static <T> ObjectGraphBuilder builderFor(Class<T> instanceClass, FieldMapper fieldMapper) throws ReflectionException {
      return new ObjectGraphBuilder(instanceClass, new DefaultObjectConstructor<T>(instanceClass).newInstance());
   }
   
   private final Map<String, ReaderGraphNode> objectGraph;
   private final FieldMapper fieldMapper;
   private final Class<T> instanceClass;
   private final T fieldOwnerInstance;

   public ObjectGraphBuilder(Class<T> instanceClass, T fieldOwnerInstance) {
      this(instanceClass, fieldOwnerInstance, StaticFieldMapper.getInstance());
   }

   public ObjectGraphBuilder(Class<T> instanceClass, T fieldOwnerInstance, FieldMapper fieldMapper) {
      this.instanceClass = instanceClass;
      this.fieldOwnerInstance = fieldOwnerInstance;
      this.fieldMapper = fieldMapper;

      objectGraph = buildObjectGraph();
   }

   private Map<String, ReaderGraphNode> buildObjectGraph() {
      final Map<String, ReaderGraphNode> partialObjectGraph = new HashMap<String, ReaderGraphNode>();

      for (Field field : instanceClass.getDeclaredFields()) {
         if (JxAnnotationTool.isInterestedInField(field)) {
            System.out.println("Mapping field: " + field.getName() + "::" + field.getType().getName() + " on class: " + instanceClass.getName());

            final MappedField mappedField = fieldMapper.mapField(field, fieldOwnerInstance);

            ObjectGraphBuilder fieldObjectBuilder = null;

            if (fieldMapper.shouldDescend(field.getType())) {
               fieldObjectBuilder = ObjectGraphBuilder.builderFor(field.getType(), fieldMapper);
            }

            partialObjectGraph.put(mappedField.getName(), new ReaderGraphNode(fieldObjectBuilder, mappedField));
         }
      }

      return partialObjectGraph;
   }


   public T getObjectInstance() {
      return fieldOwnerInstance;
   }

   public Class<T> getInstanceClass() {
      return instanceClass;
   }

   public ReaderGraphNode getField(String fieldName) {
      return objectGraph.get(fieldName);
   }
}
