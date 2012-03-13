package net.jps.jx.jackson.mapping;

import net.jps.jx.jackson.mapping.ObjectGraphBuilder;

/**
 *
 * @author zinic
 */
public interface MappedCollection {

   void add(Object obj);

   ObjectGraphBuilder newCollectionValue();
}
