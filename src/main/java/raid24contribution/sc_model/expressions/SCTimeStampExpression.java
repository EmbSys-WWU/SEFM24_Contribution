package raid24contribution.sc_model.expressions;

import org.w3c.dom.Node;

/**
 * Represents the "sc_time_stamp()"-Keyword in {@link OutputExpression}s;
 *
 */
public class SCTimeStampExpression extends MarkerExpression {
    
    private static final long serialVersionUID = -5289413288208491061L;
    
    public SCTimeStampExpression(Node n, String label) {
        super(n, label);
    }
    
    @Override
    public String toString() {
        return super.toString() + "sc_time_stamp()";
    }
    
}
