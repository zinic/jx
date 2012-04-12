package net.jps.jx;

import net.jps.jx.mapping.ClassMapper;
import net.jps.jx.mapping.ObjectConstructor;

/**
 *
 * @author zinic
 */
public class JxControlsImpl implements JxControls {

    private final ObjectConstructor objectConstructor;
    private final ClassMapper classMapper;

    public JxControlsImpl(ObjectConstructor objectConstructor, ClassMapper classMapper) {
        this.objectConstructor = objectConstructor;
        this.classMapper = classMapper;
    }

    @Override
    public ClassMapper getClassMapper() {
        return classMapper;
    }

    @Override
    public ObjectConstructor getObjectConstructor() {
        return objectConstructor;
    }
}
