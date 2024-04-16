package raid24contribution.sc_model.expressions;

import org.w3c.dom.Node;

/**
 * Represents the "name()"-Method in {@link OutputExpression}s. This method gets the name of the
 * active module instance. This cannot be determined in the SysCIR but have to be resolved after
 * transformation or at runtime.
 * 
 * 
 */
public class NameExpression extends MarkerExpression {
    
    private static final long serialVersionUID = 3543695685207328442L;
    
    public NameExpression(Node n, String label) {
        super(n, label);
    }
    
    @Override
    public String toString() {
        return super.toString() + "name()";
    }
}
