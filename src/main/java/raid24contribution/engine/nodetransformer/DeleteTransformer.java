package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.DeleteExpression;
import raid24contribution.sc_model.expressions.Expression;

/**
 * Transforms calls to 'delete $var' and generates a deleteexpression.
 * 
 */
public class DeleteTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(DeleteTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        int prevStacksize = e.getExpressionStack().size();
        handleChildNodes(node, e);
        int currStacksize = e.getExpressionStack().size();
        DeleteExpression ret = null;
        if (currStacksize == prevStacksize + 1) {
            Expression expr = e.getExpressionStack().pop();
            ret = new DeleteExpression(node);
            ret.setVarToDeleteExpr(expr);
        } else {
            logger.error("{}: Could not determine 'delete' expression ", NodeUtil.getFixedAttributes(node));
        }
        if (ret != null) {
            e.getExpressionStack().add(ret);
        } else {
            logger.error("{}: Couldn't transform 'delete' expression", NodeUtil.getFixedAttributes(node));
        }
    }
}
