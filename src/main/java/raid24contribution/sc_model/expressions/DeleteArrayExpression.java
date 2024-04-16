package raid24contribution.sc_model.expressions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Represents a 'delete [] arr' call.
 */
public class DeleteArrayExpression extends DeleteExpression {
    
    private static Logger logger = LogManager.getLogger(DeleteArrayExpression.class.getName());
    
    private static final long serialVersionUID = -474904418154664299L;
    
    public DeleteArrayExpression(Node n) {
        super(n);
    }
    
    @Override
    public String toString() {
        return DELETE + " [] " + getVarToDeleteExpr().toStringNoSem();
    }
}
