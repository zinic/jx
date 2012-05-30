package net.jps.jx.jackson.plugin;

import java.util.Stack;
import net.jps.jx.jackson.CollectionFieldEntry;
import net.jps.jx.jackson.GraphNode;
import net.jps.jx.jackson.SkipTracker;
import net.jps.jx.mapping.ClassMapper;
import net.jps.jx.mapping.ObjectConstructor;

/**
 *
 * @author zinic
 */
public class GraphContextImpl implements GraphContext {

   private final Stack<CollectionFieldEntry> collectionFieldStack;
   private final Stack<GraphNode> graphNodeStack;
   private final ObjectConstructor objectConstructor;
   private final ClassMapper classMapper;
   private final SkipTracker skipTracker;

   public GraphContextImpl(Stack<CollectionFieldEntry> collectionFieldStack, Stack<GraphNode> graphNodeStack, ObjectConstructor objectConstructor, ClassMapper classMapper, SkipTracker skipTracker) {
      this.collectionFieldStack = collectionFieldStack;
      this.graphNodeStack = graphNodeStack;
      this.objectConstructor = objectConstructor;
      this.classMapper = classMapper;
      this.skipTracker = skipTracker;
   }

   @Override
   public void skipOnce() {
      skipTracker.skipOnce();
   }
   
   @Override
   public <T> T newInstance(Class<T> clazz) {
      return objectConstructor.newInstance(clazz);
   }

   @Override
   public boolean hasMoreGraphNodes() {
      return !graphNodeStack.empty();
   }

   @Override
   public ClassMapper classMapper() {
      return classMapper;
   }

   @Override
   public CollectionFieldEntry peekCollection() {
      return collectionFieldStack.peek();
   }

   @Override
   public CollectionFieldEntry popCollection() {
      return collectionFieldStack.pop();
   }

   @Override
   public void pushCollection(CollectionFieldEntry collectionFieldEntry) {
      collectionFieldStack.push(collectionFieldEntry);
   }

   @Override
   public GraphNode peekGraphNode() {
      return graphNodeStack.peek();
   }

   @Override
   public GraphNode popGraphNode() {
      return graphNodeStack.pop();
   }

   @Override
   public void pushGraphNode(GraphNode node) {
      graphNodeStack.push(node);
   }
}
