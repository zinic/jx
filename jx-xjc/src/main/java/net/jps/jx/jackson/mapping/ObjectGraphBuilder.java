package net.jps.jx.jackson.mapping;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.jps.jx.util.reflection.JxAnnotationTool;
import net.jps.jx.util.reflection.ReflectionException;
import net.jps.jx.util.reflection.ReflectiveMappedField;

/**
 *
 * @author zinic
 */
public class ObjectGraphBuilder<T> {

   public static <T> ObjectGraphBuilder builderFor(Class<T> instanceClass) throws ReflectionException {
      try {
         final T objectInstance = instanceClass.newInstance();

         return new ObjectGraphBuilder<T>(instanceClass, objectInstance);
      } catch (IllegalAccessException iae) {
         throw new ReflectionException("Unable to access constructor for invocation. Target class: " + instanceClass.getName(), iae);
      } catch (IllegalArgumentException iae) {
         throw new ReflectionException("Illegal argument caught by underlying constrctor during reflective call. Target class: " + instanceClass.getName(), iae);
      } catch (InstantiationException ie) {
         throw new ReflectionException("Constructor invocation failed. Target class: " + instanceClass.getName(), ie);
      }
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
            final MappedField mappedField = new ReflectiveMappedField(field, objectInstance);
            final ObjectGraphBuilder fieldObjectBuilder = ObjectGraphBuilder.builderFor(field.getType());

            partialObjectGraph.put(mappedField.getName(), new ObjectGraphNode(fieldObjectBuilder, mappedField));
         }
      }

      return partialObjectGraph;
   }

   public ObjectGraphNode getField(String fieldName) {
      return objectGraph.get(fieldName);
   }
}
