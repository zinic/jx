package net.jps.jx.util.xml;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 *
 * @author zinic
 */
public final class DatatypeFactoryManager {

   private static final DatatypeFactory DATATYPE_FACTORY_INSTANCE;

   static {
      try {
         DATATYPE_FACTORY_INSTANCE = DatatypeFactory.newInstance();
      } catch (DatatypeConfigurationException configurationException) {
         throw new DatatypeFactoryManagerException("Failed to init a datatype factory for XML datatypes.", configurationException);
      }
   }

   public static DatatypeFactory getInstance() {
      return DATATYPE_FACTORY_INSTANCE;
   }

   private DatatypeFactoryManager() {
   }
}
