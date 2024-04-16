package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.EndlineExpression;

/**
 * Handles the standart_manipulator nodes. At the moment, only "endl" is known to be put in this
 * node.
 *
 */
public class StandartManipulatorTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(StandartManipulatorTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        String name = NodeUtil.getAttributeValueByName(node, "name");
        if (name.equals("endl")) {
            EndlineExpression ele = new EndlineExpression(node, "");
            
            e.getExpressionStack().add(ele);
        } else {
            // we have a standard manipulator we cannot handle
            logger.error("{}: Encountered a standart_manipulator node with name {} which cannot be handled (yet).",
                    NodeUtil.getFixedAttributes(node), name);
        }
    }
}
