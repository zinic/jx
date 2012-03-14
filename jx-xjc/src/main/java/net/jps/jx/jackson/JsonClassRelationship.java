package net.jps.jx.jackson;

import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author zinic
 */
public abstract class JsonClassRelationship {

   private final JsonType jsonType;
   private final Class javaClass;

   public JsonClassRelationship(JsonType jsonType, Class javaClass) {
      this.jsonType = jsonType;
      this.javaClass = javaClass;
   }

   public abstract void write(JsonGenerator jsonGenerator);
}
