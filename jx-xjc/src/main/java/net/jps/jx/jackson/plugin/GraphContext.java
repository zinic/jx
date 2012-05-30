package net.jps.jx.jackson.plugin;

import net.jps.jx.jackson.CollectionFieldEntry;
import net.jps.jx.jackson.GraphNode;
import net.jps.jx.mapping.ClassMapper;

/**
 *
 * @author zinic
 */
public interface GraphContext {

   void skipOnce();
   
   boolean hasMoreGraphNodes();

   <T> T newInstance(Class<T> clazz);

   ClassMapper classMapper();

   void pushCollection(CollectionFieldEntry collectionFieldEntry);

   CollectionFieldEntry peekCollection();

   CollectionFieldEntry popCollection();

   void pushGraphNode(GraphNode node);

   GraphNode peekGraphNode();

   GraphNode popGraphNode();
}
