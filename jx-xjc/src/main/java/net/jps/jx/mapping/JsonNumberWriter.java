package net.jps.jx.mapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import net.jps.jx.JxWritingException;

/**
 *
 * @author zinic
 */
public interface JsonNumberWriter {

   void writeNumber(String string) throws IOException, JxWritingException;

   void writeNumber(BigDecimal bd) throws IOException, JxWritingException;

   void writeNumber(float f) throws IOException, JxWritingException;

   void writeNumber(double d) throws IOException, JxWritingException;

   void writeNumber(BigInteger bi) throws IOException, JxWritingException;

   void writeNumber(long l) throws IOException, JxWritingException;

   void writeNumber(int i) throws IOException, JxWritingException;
}
