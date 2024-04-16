package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;

/**
 * we prepare the environment for the parameter-parsing
 *
 */
public class ParameterListTransformer extends AbstractNodeTransformer {
    
    
    @Override
    public void transformNode(Node n, Environment e) {
        e.getLastParameterList().clear();
        handleChildNodes(n, e);
    }
    
}
