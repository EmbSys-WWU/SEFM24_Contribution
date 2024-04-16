package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.BreakExpression;

/**
 * this class only creates a new Breakstatmentexpression<
 * 
 * 
 */
public class BreakStatementTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        
        BreakExpression be = new BreakExpression(node);
        e.getExpressionStack().add(be);
    }
}
