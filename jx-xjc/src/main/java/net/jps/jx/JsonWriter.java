package net.jps.jx;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author zinic
 */
public interface JsonWriter<T> {

   void write(T rootObject, OutputStream outputStream) throws IOException, JxWritingException;
}
