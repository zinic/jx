package net.jps.jx.jackson.mapping;

/**
 *
 * @author zinic
 */
public interface MappedCollection {

   void add(Object obj);

   Class getCollectionValueClass();
}
