package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;

/**
 * Fallback node transformer. Ignores the actual node and handle all child nodes.
 * 
 */
public class DefaultTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        handleChildNodes(node, e);
        
    }
    
}
