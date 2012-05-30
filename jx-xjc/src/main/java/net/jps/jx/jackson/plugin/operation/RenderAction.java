package net.jps.jx.jackson.plugin.operation;

/**
 *
 * @author zinic
 */
public class RenderAction {

   private final RenderOperation operation;
   private final Object operationPayload;

   public RenderAction(RenderOperation operation) {
      this(operation, null);
   }

   public RenderAction(RenderOperation operation, Object operationPayload) {
      this.operationPayload = operationPayload;
      this.operation = operation;
   }

   public Object payload() {
      return operationPayload;
   }

   public boolean hasPayload() {
      return operationPayload != null;
   }

   public RenderOperation operation() {
      return operation;
   }
}
