package net.jps.jx;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author zinic
 */
public interface JsonReader<T> {

   T read(InputStream is) throws IOException, JxParsingException;
}
