package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCEnumElement;

/**
 * This expression represents the use of an element declared in an enum.
 * 
 * 
 */
public class EnumElementExpression extends Expression {
    
    private static final long serialVersionUID = 5840966780576556321L;
    
    private SCEnumElement enumElem;
    
    public EnumElementExpression(Node n, SCEnumElement val) {
        super(n);
        this.enumElem = val;
    }
    
    public void setEnumElement(SCEnumElement value) {
        this.enumElem = value;
    }
    
    public SCEnumElement getEnumElement() {
        return this.enumElem;
    }
    
    @Override
    public String toString() {
        return this.enumElem.getName();
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        return ret;
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
