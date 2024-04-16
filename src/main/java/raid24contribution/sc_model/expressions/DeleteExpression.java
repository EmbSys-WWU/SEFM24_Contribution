package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * Represents a 'delete ptr' call.
 * 
 */
public class DeleteExpression extends Expression {
    
    private static Logger logger = LogManager.getLogger(DeleteExpression.class.getName());
    
    private Expression varToDeleteExpr;
    private static final long serialVersionUID = -81727930449111393L;
    protected static final String DELETE = "delete";
    
    public DeleteExpression(Node n) {
        super(n);
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ll = new LinkedList<>();
        ll.add(this.varToDeleteExpr);
        return ll;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.varToDeleteExpr;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return 1;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.varToDeleteExpr = replaceSingleExpression(this.varToDeleteExpr, replacements);
    }
    
    @Override
    public String toString() {
        return DELETE + " " + this.varToDeleteExpr.toStringNoSem();
    }
    
    /**
     * @return the varToDeleteExpr
     */
    public Expression getVarToDeleteExpr() {
        return this.varToDeleteExpr;
    }
    
    /**
     * @param varToDeleteExpr the varToDeleteExpr to set
     */
    public void setVarToDeleteExpr(Expression varToDeleteExpr) {
        varToDeleteExpr.setParent(this);
        this.varToDeleteExpr = varToDeleteExpr;
    }
}
