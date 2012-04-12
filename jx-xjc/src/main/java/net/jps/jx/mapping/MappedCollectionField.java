package net.jps.jx.mapping;

import net.jps.jx.mapping.method.CollectionAddMethod;

/**
 *
 * @author zinic
 */
public interface MappedCollectionField extends MappedField {

    CollectionAddMethod addMethodFor(Object addTarget);

    Class getCollectionValueClass();
}
