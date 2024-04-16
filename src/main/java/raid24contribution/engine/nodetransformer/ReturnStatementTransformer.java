package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.ReturnExpression;

/**
 * we handle the childnode and get the top expression from the stack, this is the returned value if
 * their wasn't an expression nothing is returned then we create a new returnExpression and add it
 * to the stack
 * 
 */
public class ReturnStatementTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        int size = e.getExpressionStack().size();
        handleChildNodes(node, e);
        Expression ret;
        if (size == e.getExpressionStack().size()) {
            ret = null;
        } else {
            ret = e.getExpressionStack().pop();
        }
        
        ReturnExpression re = new ReturnExpression(node, ret);
        e.getExpressionStack().push(re);
    }
}
