package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * This abstract class is used as a base class for all marker expressions. A marker expression is an
 * expression which contains no information but the expression itself. An example for a
 * markerExpression is the "break"-statement in loops or switch-case-blocks.
 * 
 * 
 */
public abstract class MarkerExpression extends Expression {
    
    private static final long serialVersionUID = -6603730384359041400L;
    
    public MarkerExpression(Node n, String label) {
        super(n, label);
    }
    
    public MarkerExpression(Node n) {
        super(n);
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        return new LinkedList<>();
    }
    
    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return 0;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {}
}
