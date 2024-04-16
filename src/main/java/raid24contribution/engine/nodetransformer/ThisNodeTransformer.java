package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.SCClassInstanceExpression;
import raid24contribution.sc_model.variables.SCClassInstance;

/**
 * Handles the "this" node, which is used inside of classes to reference the current class instance.
 * 
 */
public class ThisNodeTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(ThisNodeTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        Expression ce = null;
        if (e.getCurrentClass() != null) {
            SCClassInstance clI = new SCClassInstance("this", e.getCurrentClass(), e.getCurrentClass());
            ce = new SCClassInstanceExpression(node, clI);
            e.getExpressionStack().add(ce);
        } else {
            logger.error("{}: Encountered a this node outside of a struct or sc_module.",
                    NodeUtil.getFixedAttributes(node));
        }
    }
}
