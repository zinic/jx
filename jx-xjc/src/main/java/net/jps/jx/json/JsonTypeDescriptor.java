package net.jps.jx.json;

import net.jps.jx.json.JsonType;

/**
 *
 * @author zinic
 */
public class JsonTypeDescriptor {

   private final Class javaClass;
   private final JsonType jsonType;

   public JsonTypeDescriptor(Class javaClass, JsonType jsonType) {
      this.javaClass = javaClass;
      this.jsonType = jsonType;
   }

   public Class getJavaClass() {
      return javaClass;
   }

   public JsonType getJsonType() {
      return jsonType;
   }
}
