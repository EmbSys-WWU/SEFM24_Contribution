package raid24contribution.sc_model.expressions;

import org.w3c.dom.Node;

public class SCStopExpression extends MarkerExpression {
    
    private static final long serialVersionUID = -8474878549101997749L;
    
    public SCStopExpression(Node n, String label) {
        super(n, label);
    }
    
    @Override
    public String toString() {
        return super.toString() + "sc_stop()";
    }
    
}
