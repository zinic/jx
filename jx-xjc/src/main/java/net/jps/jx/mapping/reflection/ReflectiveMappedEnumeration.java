package net.jps.jx.mapping.reflection;

import java.lang.reflect.Field;
import net.jps.jx.mapping.method.SetterMethod;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedEnumeration extends ReflectiveMappedField {

    public ReflectiveMappedEnumeration(Field fieldRef) {
        super(fieldRef);
    }

    @Override
    public SetterMethod getSetterFor(final Object setterTarget) {
        final SetterMethod enumerationSetter = super.getSetterFor(setterTarget);

        return new SetterMethod() {

            @Override
            public void set(Object enumerationValue) {
                if (!(enumerationValue instanceof String)) {
                    throw new IllegalArgumentException("Enumerations may only be set with their string representations.");
                }

                final Class fieldType = getField().getType();
                final Enum[] enumConstants = (Enum[]) fieldType.getEnumConstants();

                if (enumConstants != null && enumConstants.length > 0) {
                    for (Enum enumConstant : enumConstants) {
                        if (enumConstant.name().equals(enumerationValue)) {
                            enumerationSetter.set(enumConstant);
                            return;
                        }
                    }
                }

                throw new ReflectionException("Unable to map enumeration value: " + enumerationValue + " to enumeration class: " + fieldType.getName());
            }
        };
    }
}
