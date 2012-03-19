package net.jps.jx.jackson.mapping;

import net.jps.jx.mapping.JsonNumberWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import net.jps.jx.JxWritingException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author zinic
 */
public class JacksonNumberWriter implements JsonNumberWriter {

   private final JsonGenerator gen;

   public JacksonNumberWriter(JsonGenerator gen) {
      this.gen = gen;
   }
   
   private JxWritingException handleJsonGenerationException(JsonGenerationException jge) {
      return new JxWritingException("Failed to write number. Reason: " + jge.getMessage(), jge);
   }

   @Override
   public void writeNumber(String string) throws IOException, JxWritingException {
      try {
         gen.writeNumber(string);
      } catch (JsonGenerationException jge) {
         throw handleJsonGenerationException(jge);
      }
   }

   @Override
   public void writeNumber(BigDecimal bd) throws IOException, JxWritingException {
      try {
         gen.writeNumber(bd);
      } catch (JsonGenerationException jge) {
         throw handleJsonGenerationException(jge);
      }
   }

   @Override
   public void writeNumber(float f) throws IOException, JxWritingException {
      try {
         gen.writeNumber(f);
      } catch (JsonGenerationException jge) {
         throw handleJsonGenerationException(jge);
      }
   }

   @Override
   public void writeNumber(double d) throws IOException, JxWritingException {
      try {
         gen.writeNumber(d);
      } catch (JsonGenerationException jge) {
         throw handleJsonGenerationException(jge);
      }
   }

   @Override
   public void writeNumber(BigInteger bi) throws IOException, JxWritingException {
      try {
         gen.writeNumber(bi);
      } catch (JsonGenerationException jge) {
         throw handleJsonGenerationException(jge);
      }
   }

   @Override
   public void writeNumber(long l) throws IOException, JxWritingException {
      try {
         gen.writeNumber(l);
      } catch (JsonGenerationException jge) {
         throw handleJsonGenerationException(jge);
      }
   }

   @Override
   public void writeNumber(int i) throws IOException, JxWritingException {
      try {
         gen.writeNumber(i);
      } catch (JsonGenerationException jge) {
         throw handleJsonGenerationException(jge);
      }
   }
}
