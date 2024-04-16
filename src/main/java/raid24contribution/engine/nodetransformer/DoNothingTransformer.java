package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;

/**
 * This node transformer is used for all nodes containing no relevant information.
 * 
 */
public class DoNothingTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        return;
        
    }
    
}
