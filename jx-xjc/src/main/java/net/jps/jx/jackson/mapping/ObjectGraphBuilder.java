package net.jps.jx.jackson.mapping;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.jps.jx.util.reflection.JxAnnotationTool;
import net.jps.jx.util.reflection.ReflectionException;
import net.jps.jx.util.reflection.ReflectiveMappedCollection;
import net.jps.jx.util.reflection.ReflectiveMappedField;

/**
 *
 * @author zinic
 */
public class ObjectGraphBuilder<T> {

   public static <T> ObjectGraphBuilder builderFor(Class<T> instanceClass) throws ReflectionException {
      return new ObjectGraphBuilder(instanceClass, new DefaultObjectConstructor<T>(instanceClass).newInstance());
   }
   
   private final Map<String, ObjectGraphNode> objectGraph;
   private final Class<T> instanceClass;
   private final T objectInstance;

   public ObjectGraphBuilder(Class<T> instanceClass, T objectInstance) {
      this.instanceClass = instanceClass;
      this.objectInstance = objectInstance;

      objectGraph = buildObjectGraph();
   }

   private Map<String, ObjectGraphNode> buildObjectGraph() {
      final Map<String, ObjectGraphNode> partialObjectGraph = new HashMap<String, ObjectGraphNode>();

      for (Field field : instanceClass.getDeclaredFields()) {
         if (JxAnnotationTool.isInterestedInField(field)) {
            final MappedField mappedField = Collection.class.isAssignableFrom(field.getType()) ? new ReflectiveMappedCollection(field, objectInstance) : new ReflectiveMappedField(field, objectInstance);
            final ObjectGraphBuilder fieldObjectBuilder = ObjectGraphBuilder.builderFor(field.getType());

            partialObjectGraph.put(mappedField.getName(), new ObjectGraphNode(fieldObjectBuilder, mappedField));
         }
      }

      return partialObjectGraph;
   }

   public T getObjectInstance() {
      return objectInstance;
   }

   public Class<T> getInstanceClass() {
      return instanceClass;
   }

   public ObjectGraphNode getField(String fieldName) {
      return objectGraph.get(fieldName);
   }
}
