package net.jps.jx.jackson.mapping;

/**
 *
 * @author zinic
 */
public interface MappedField {

   String getName();

   Object get();

   boolean hasGetter();

   void set(Object value);

   boolean hasSetter();

   boolean shouldDescend();

   boolean canSet();
}
