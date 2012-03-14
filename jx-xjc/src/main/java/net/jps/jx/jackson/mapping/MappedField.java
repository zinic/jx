package net.jps.jx.jackson.mapping;

/**
 *
 * @author zinic
 */
public interface MappedField {

   Class getFieldType();
   
   String getName();

   Object get();

   boolean hasGetter();

   void set(Object value);

   boolean hasSetter();

   boolean canSet();
}
