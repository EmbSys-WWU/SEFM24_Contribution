package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;

/**
 * Handle all declared local variables or members defined in a list (ex: 'int x, y;')
 *
 */
public class DeclaratorListTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.getLastType().clear();
        handleChildNodes(node, e);
        e.getLastType().clear();
    }
}
