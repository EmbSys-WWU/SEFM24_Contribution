package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;

/**
 * This transformer handles the right part in a binary expression like 'int x = 3;' (e.g. '3' is the
 * expression).
 *
 */
public class InitializerTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(InitializerTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.setLastInitializer(null);
        int size = e.getExpressionStack().size();
        handleChildNodes(node, e);
        if (e.getExpressionStack().size() == size + 1) {
            e.setLastInitializer(e.getExpressionStack().pop());
        } else if (e.getExpressionStack().size() == size) {
            e.setLastInitializer(null);
        } else {
            logger.error("{}: more then one Expression as Initializer", NodeUtil.getFixedAttributes(node));
        }
    }
}
