package raid24contribution.sc_model.expressions;

import org.w3c.dom.Node;

/**
 * this expression represents the "continue"-Construct that is used for Example in
 * Switch-Case-Cosntructs
 *
 */
public class ContinueExpression extends MarkerExpression {
    
    private static final long serialVersionUID = -7081142269547806405L;
    
    public ContinueExpression(Node n) {
        super(n);
    }
    
    @Override
    public String toString() {
        return super.toString() + "continue;";
    }
    
}
