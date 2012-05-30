package net.jps.jx.jackson.plugin;

import java.util.List;
import net.jps.jx.jackson.plugin.operation.RenderAction;

/**
 *
 * @author zinic
 */
public interface RenderResult {

   RenderChainAction chainAction();

   boolean hasRenderActions();

   List<RenderAction> renderActions();
}
