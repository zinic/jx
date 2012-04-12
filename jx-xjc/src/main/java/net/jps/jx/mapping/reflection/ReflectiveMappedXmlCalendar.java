package net.jps.jx.mapping.reflection;

import java.lang.reflect.Field;
import javax.xml.datatype.DatatypeFactory;
import net.jps.jx.mapping.method.SetterMethod;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedXmlCalendar extends ReflectiveMappedField {

    private final DatatypeFactory datatypeFactory;

    public ReflectiveMappedXmlCalendar(Field fieldRef, DatatypeFactory datatypeFactory) {
        super(fieldRef);

        this.datatypeFactory = datatypeFactory;
    }

    @Override
    public SetterMethod getSetterFor(Object setterTarget) {
        final SetterMethod callendarSetter = super.getSetterFor(setterTarget);

        return new SetterMethod() {

            @Override
            public void set(Object dateString) {
                if (!(dateString instanceof String)) {
                    throw new IllegalArgumentException("XML dates may only be set with their string representations.");
                }

                callendarSetter.set(datatypeFactory.newXMLGregorianCalendar((String) dateString));
            }
        };
    }
}
