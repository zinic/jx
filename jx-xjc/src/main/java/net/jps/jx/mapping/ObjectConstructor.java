package net.jps.jx.mapping;

/**
 *
 * @author zinic
 */
public interface ObjectConstructor {

    <T> T newInstance(Class<T> clazz);
}
