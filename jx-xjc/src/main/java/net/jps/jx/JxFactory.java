package net.jps.jx;

/**
 *
 * @author zinic
 */
public interface JxFactory {

    <T> JsonReader<T> newReader(Class<T> c);

    <T> JsonWriter<T> newWriter(Class<T> c);
}
