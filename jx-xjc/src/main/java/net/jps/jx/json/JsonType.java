package net.jps.jx.json;

/**
 *
 * @author zinic
 */
public enum JsonType {

   STRING,
   BOOLEAN,
   ARRAY,
   NUMBER,
   OBJECT,
   NULL;

   public static boolean isCollection(JsonTypeDescriptor typeDescriptor) {
      return isCollection(typeDescriptor.getJsonType());
   }

   public static boolean isCollection(JsonType type) {
      return type == ARRAY;
   }
}
