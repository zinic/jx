package net.jps.jx.mapping.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import net.jps.jx.annotation.JsonFieldName;
import net.jps.jx.mapping.MappedField;
import net.jps.jx.jaxb.JaxbConstants;
import net.jps.jx.mapping.method.AccessorMethod;
import net.jps.jx.mapping.method.SetterMethod;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedField implements MappedField {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final XmlAttribute xmlAttributeAnnotation;
    private final XmlElement xmlElementAnnotation;
    private final JsonFieldName jsonFieldAnnotation;
    private final Field fieldRef;
    private final Method getterRef, setterRef;

    public ReflectiveMappedField(Field fieldRef) {
        this.fieldRef = fieldRef;

        xmlElementAnnotation = (XmlElement) fieldRef.getAnnotation(XmlElement.class);
        xmlAttributeAnnotation = (XmlAttribute) fieldRef.getAnnotation(XmlAttribute.class);
        jsonFieldAnnotation = (JsonFieldName) fieldRef.getAnnotation(JsonFieldName.class);

        getterRef = findGetter();
        setterRef = findSetter();
    }

    @Override
    public Field getField() {
        return fieldRef;
    }

    @Override
    public AccessorMethod getAccessorFor(final Object getterTarget) {
        return new AccessorMethod() {

            @Override
            public Object get() {
                return invoke(getterTarget, getterRef);
            }
        };
    }

    @Override
    public SetterMethod getSetterFor(final Object setterTarget) {
        if (!hasSetter()) {
            throw new UnsupportedOperationException("Setter method not supported.");
        }

        return new SetterMethod() {

            @Override
            public void set(Object value) {
                invoke(setterTarget, setterRef, value);
            }
        };
    }

    @Override
    public boolean hasGetter() {
        return getterRef != null;
    }

    @Override
    public boolean hasSetter() {
        return setterRef != null;
    }

    @Override
    public String getName() {
        return jsonFieldAnnotation != null ? jsonFieldAnnotation.value() : getJaxbNameForField();
    }

    private String getJaxbNameForField() {
        if (xmlAttributeAnnotation != null) {
            return JaxbConstants.JAXB_DEFAULT_NAME.equals(xmlAttributeAnnotation.name()) ? fieldRef.getName() : xmlAttributeAnnotation.name();
        }

        if (xmlElementAnnotation != null) {
            return JaxbConstants.JAXB_DEFAULT_NAME.equals(xmlElementAnnotation.name()) ? fieldRef.getName() : xmlElementAnnotation.name();
        }

        return fieldRef.getName();
    }

    private Method findGetter() {
        final String getterMethodName = formatGetterMethodName(fieldRef.getName(), fieldRef.getType());

        for (Method method : fieldRef.getDeclaringClass().getMethods()) {
            if (method.getName().equals(getterMethodName)) {
                return method;
            }
        }

        throw new ReflectionException("Unable to find getter method: " + getterMethodName + "() for field: " + fieldRef.getName() + " with class: " + fieldRef.getDeclaringClass().getName());
    }

    private Method findSetter() {
        final String setterMethodName = formatSetterMethodName(fieldRef.getName());

        for (Method method : fieldRef.getDeclaringClass().getMethods()) {
            if (method.getName().equals(setterMethodName)) {
                return method;
            }
        }

        return null;
    }

    private String formatGetterMethodName(String name, Class fieldType) {
        final StringBuilder methodNameBuilder = formatFieldName(name);

        final String getterPrefix = fieldType.equals(boolean.class) || fieldType.equals(Boolean.class) ? "is" : "get";
        methodNameBuilder.insert(0, getterPrefix);

        return methodNameBuilder.toString();
    }

    private String formatSetterMethodName(String name) {
        final StringBuilder methodNameBuilder = formatFieldName(name);
        methodNameBuilder.insert(0, "set");

        return methodNameBuilder.toString();
    }

    private StringBuilder formatFieldName(String name) {
        final StringBuilder nameBuilder = new StringBuilder(name);

        // JAXB espcaes field names that clash with reserved keywords with a '_' character
        if (nameBuilder.charAt(0) == '_') {
            nameBuilder.deleteCharAt(0);
        }

        final char fieldNameFirstChar = nameBuilder.charAt(0);

        if (Character.isLowerCase(fieldNameFirstChar)) {
            nameBuilder.setCharAt(0, Character.toUpperCase(fieldNameFirstChar));
        }

        return nameBuilder;
    }

    protected Object invoke(Object target, Method m) {
        return invoke(target, m, EMPTY_OBJECT_ARRAY);
    }

    protected Object invoke(Object target, Method m, Object... args) {
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException iae) {
            throw new ReflectionException("Unable to access method for invocation. Target method: " + m.getName(), iae);
        } catch (IllegalArgumentException iae) {
            throw new ReflectionException("Illegal argument caught by underlying method during reflective call. Target method: " + m.getName(), iae);
        } catch (InvocationTargetException ite) {
            throw new ReflectionException("Method invocation failed. Target method: " + m.getName(), ite);
        }
    }
}
