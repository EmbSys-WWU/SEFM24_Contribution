package raid24contribution.sc_model.expressions;

import org.w3c.dom.Node;

/**
 * Represents the "sc_delta_count()"-Keyword in {@link OutputExpression}s;
 *
 */
public class SCDeltaCountExpression extends MarkerExpression {
    
    private static final long serialVersionUID = -2056643610429896197L;
    
    public SCDeltaCountExpression(Node n, String label) {
        super(n, label);
    }
    
    @Override
    public String toString() {
        return super.toString() + "sc_delta_count()";
    }
}
