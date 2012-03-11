package net.jps.jx.jackson.mapping;

/**
 *
 * @author zinic
 */
public interface MappedField {

   String getName();

   Object get();

   void set(Object value);

   boolean isCollection();

   boolean canSet();
}
