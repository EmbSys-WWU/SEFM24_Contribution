package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.ContinueExpression;

/**
 * this class only creates a new Continuestatmentexpression<
 * 
 * 
 */
public class ContinueStatementTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        
        ContinueExpression be = new ContinueExpression(node);
        e.getExpressionStack().add(be);
    }
}
