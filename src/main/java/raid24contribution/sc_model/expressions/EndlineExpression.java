package raid24contribution.sc_model.expressions;

import org.w3c.dom.Node;

/**
 * Represents the "endl"-Keyword in {@link OutputExpression}s;
 *
 */
public class EndlineExpression extends MarkerExpression {
    
    private static final long serialVersionUID = 1311652348198370342L;
    
    public EndlineExpression(Node n, String label) {
        super(n, label);
    }
    
    @Override
    public String toString() {
        return "endl";
    }
}
