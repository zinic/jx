package net.jps.jx.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public class DefaultObjectConstructor implements ObjectConstructor {

    private final DatatypeFactory datatypeFactory;

    public DefaultObjectConstructor(DatatypeFactory datatypeFactory) {
        this.datatypeFactory = datatypeFactory;
    }

    @Override
    public <T> T newInstance(Class<T> instanceClass) {
        try {
            if (isPrimative(instanceClass)) {
                return constructPrimativeWrapper(instanceClass);
            } else if (instanceClass.isInterface()) {
                if (List.class.equals(instanceClass)) {
                    return (T) new ArrayList();
                } else if (Collection.class.equals(instanceClass)) {
                    return (T) new HashSet();
                } else {
                    throw new ReflectionException("Unable to create instance for interface: " + instanceClass.getName());
                }
            } else if (Enum.class.isAssignableFrom(instanceClass)) {
                final T[] enumConstants = instanceClass.getEnumConstants();

                return enumConstants[0];
            } else if (XMLGregorianCalendar.class.equals(instanceClass)) {
                return (T) datatypeFactory.newXMLGregorianCalendar();
            } else {
                return instanceClass.newInstance();
            }
        } catch (IllegalAccessException iae) {
            throw new ReflectionException("Unable to access constructor for invocation. Target class: " + instanceClass.getName(), iae);
        } catch (IllegalArgumentException iae) {
            throw new ReflectionException("Illegal argument caught by underlying constrctor during reflective call. Target class: " + instanceClass.getName(), iae);
        } catch (InstantiationException ie) {
            throw new ReflectionException("Constructor invocation failed. Target class: " + instanceClass.getName(), ie);
        }
    }

    public <T> T constructPrimativeWrapper(Class<T> instanceClass) {
        Object instanceObject = null;

        if (float.class.equals(instanceClass) || Float.class.equals(instanceClass)) {
            instanceObject = Double.valueOf(0);
        } else if (double.class.equals(instanceClass) || Double.class.equals(instanceClass)) {
            instanceObject = Double.valueOf(0);
        } else if (short.class.equals(instanceClass) || Short.class.equals(instanceClass)) {
            instanceObject = Double.valueOf(0);
        } else if (int.class.equals(instanceClass) || Integer.class.equals(instanceClass)) {
            instanceObject = Integer.valueOf(0);
        } else if (long.class.equals(instanceClass) || Long.class.equals(instanceClass)) {
            instanceObject = Long.valueOf(0);
        } else if (byte.class.equals(instanceClass) || byte.class.equals(instanceClass)) {
            instanceObject = Byte.valueOf((byte) 0);
        } else if (char.class.equals(instanceClass) || char.class.equals(instanceClass)) {
            instanceObject = Character.valueOf((char) 0);
        } else if (boolean.class.equals(instanceClass) || Boolean.class.equals(instanceClass)) {
            instanceObject = Boolean.FALSE;
        }
        return (T) instanceObject;
    }

    public boolean isPrimative(Class instanceClass) {
        boolean primative = false;

        if (float.class.equals(instanceClass) || Float.class.equals(instanceClass)) {
            primative = true;
        } else if (double.class.equals(instanceClass) || Double.class.equals(instanceClass)) {
            primative = true;
        } else if (short.class.equals(instanceClass) || Short.class.equals(instanceClass)) {
            primative = true;
        } else if (int.class.equals(instanceClass) || Integer.class.equals(instanceClass)) {
            primative = true;
        } else if (long.class.equals(instanceClass) || Long.class.equals(instanceClass)) {
            primative = true;
        } else if (byte.class.equals(instanceClass) || byte.class.equals(instanceClass)) {
            primative = true;
        } else if (char.class.equals(instanceClass) || char.class.equals(instanceClass)) {
            primative = true;
        } else if (boolean.class.equals(instanceClass) || Boolean.class.equals(instanceClass)) {
            primative = true;
        }

        return primative;
    }
}
