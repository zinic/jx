package net.jps.jx.mapping;

import net.jps.jx.mapping.method.AccessorMethod;
import net.jps.jx.mapping.method.SetterMethod;
import java.lang.reflect.Field;

/**
 *
 * @author zinic
 */
public interface MappedField {

    String getName();
    
    Field getField();

    boolean hasGetter();

    boolean hasSetter();

    SetterMethod getSetterFor(Object setterTarget);

    AccessorMethod getAccessorFor(Object accessorTarget);
}
