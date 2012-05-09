package net.jps.jx.xjc.strategy.result;

/**
 *
 * @author zinic
 */
public class JxBindingResult implements BindingResult {

   private static final BindingResult SUCCESS_INSTANCE = new JxBindingResult(null, true);
   
   public static BindingResult success() {
      return SUCCESS_INSTANCE;
   }
   
   public static BindingResult failure(String resultMessage) {
      return new JxBindingResult(resultMessage, false);
   }
   
   private final String resultMessage;
   private final boolean successful;

   private JxBindingResult(String resultMessage, boolean successful) {
      this.resultMessage = resultMessage;
      this.successful = successful;
   }

   @Override
   public String message() {
      return resultMessage;
   }

   @Override
   public boolean successful() {
      return successful;
   }
}
