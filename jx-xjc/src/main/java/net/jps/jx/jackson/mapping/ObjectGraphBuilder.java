package net.jps.jx.jackson.mapping;

import com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.jps.jx.util.reflection.*;

/**
 *
 * @author zinic
 */
public class ObjectGraphBuilder<T> {

   private static final DatatypeFactory DEFAULT_DATATYPE_FACTORY = new DatatypeFactoryImpl();
   
   public static <T> ObjectGraphBuilder builderFor(Class<T> instanceClass) throws ReflectionException {
      return new ObjectGraphBuilder(instanceClass, new DefaultObjectConstructor<T>(instanceClass).newInstance());
   }
   
   private final Map<String, ObjectGraphNode> objectGraph;
   private final DatatypeFactory datatypeFactory;
   private final Class<T> instanceClass;
   private final T objectInstance;

   public ObjectGraphBuilder(Class<T> instanceClass, T objectInstance) {
      this(instanceClass, objectInstance, DEFAULT_DATATYPE_FACTORY);
   }

   public ObjectGraphBuilder(Class<T> instanceClass, T objectInstance, DatatypeFactory datatypeFactory) {
      this.instanceClass = instanceClass;
      this.objectInstance = objectInstance;
      this.datatypeFactory = datatypeFactory;

      objectGraph = buildObjectGraph();
   }

   private Map<String, ObjectGraphNode> buildObjectGraph() {
      final Map<String, ObjectGraphNode> partialObjectGraph = new HashMap<String, ObjectGraphNode>();

      for (Field field : instanceClass.getDeclaredFields()) {
         if (JxAnnotationTool.isInterestedInField(field)) {
            System.out.println("Mapping field: " + field.getName() + "::" + field.getType().getName() + " on class: " + instanceClass.getName());

            final MappedField mappedField = mapField(field);

            ObjectGraphBuilder fieldObjectBuilder = null;

            if (mappedField.shouldDescend()) {
               fieldObjectBuilder = ObjectGraphBuilder.builderFor(field.getType());
            }

            partialObjectGraph.put(mappedField.getName(), new ObjectGraphNode(fieldObjectBuilder, mappedField));
         }
      }

      return partialObjectGraph;
   }

   private MappedField mapField(Field field) {
      final Class fieldType = field.getType();

      if (Collection.class.isAssignableFrom(fieldType)) {
         return new ReflectiveMappedCollection(field, objectInstance);
      } else if (Enum.class.isAssignableFrom(fieldType)) {
         return new ReflectiveMappedEnumeration(field, objectInstance);
      } else if (XMLGregorianCalendar.class.isAssignableFrom(fieldType)) {
         return new ReflectiveMappedXmlCalendar(field, objectInstance, datatypeFactory);
      } else {
         return new ReflectiveMappedField(field, objectInstance);
      }
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
