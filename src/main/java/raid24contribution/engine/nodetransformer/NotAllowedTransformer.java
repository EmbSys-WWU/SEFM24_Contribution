package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;

/**
 * This transformer only prints an error message that says that it is not allowed to be used.
 * Intended to be assigned to those nodes that represent SystemC constructs which are not supported.
 * 
 */
public class NotAllowedTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(NotAllowedTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        logger.error("{}: This node can not be translated.", NodeUtil.getFixedAttributes(node));
    }
    
}
