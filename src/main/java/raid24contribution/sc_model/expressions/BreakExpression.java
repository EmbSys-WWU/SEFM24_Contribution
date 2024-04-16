package raid24contribution.sc_model.expressions;

import org.w3c.dom.Node;

/**
 * this Expression represents the "break"-construct, for example in Switch-Case-Constructs
 * 
 * 
 */
public class BreakExpression extends MarkerExpression {
    
    private static final long serialVersionUID = -3052817320741779677L;
    
    public BreakExpression(Node n) {
        super(n);
    }
    
    @Override
    public String toString() {
        return super.toString() + "break;";
    }
    
}
