package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.DeleteArrayExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.SCVariableExpression;

/**
 * Transforms calls to 'delete [] $var' and generates a deletearrayexpression.
 *
 */
public class DeleteArrayTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(DeleteArrayTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        int prevStacksize = e.getExpressionStack().size();
        handleChildNodes(node, e);
        int currStacksize = e.getExpressionStack().size();
        DeleteArrayExpression ret = null;
        if (currStacksize == prevStacksize + 1) {
            Expression expr = e.getExpressionStack().pop();
            if (expr instanceof SCVariableExpression) {
                ret = new DeleteArrayExpression(node);
                ret.setVarToDeleteExpr(expr);
            } else {
                logger.error("Could not determine array to delete from expr {}", expr);
            }
        } else {
            logger.error("{}: Could not determine 'delete []' expression ", NodeUtil.getFixedAttributes(node));
        }
        if (ret != null) {
            e.getExpressionStack().add(ret);
        } else {
            logger.error("{}: Couldn't transform 'delete []' expression", NodeUtil.getFixedAttributes(node));
        }
    }
}
