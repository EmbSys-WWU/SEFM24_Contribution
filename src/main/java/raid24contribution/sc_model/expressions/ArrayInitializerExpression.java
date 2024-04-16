package raid24contribution.sc_model.expressions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression represents an Initializeation of an array out of the SystemC-Code it contains an
 * array of Expressions which represents the dimension-values
 * 
 * 
 */
public class ArrayInitializerExpression extends Expression {
    
    private static final long serialVersionUID = -4430516385046264470L;
    
    protected Expression[] values = null;
    
    public ArrayInitializerExpression(Node n, int dim) {
        super(n);
        this.values = new Expression[dim];
    }
    
    public void initAtPosition(int pos, Expression val) {
        this.values[pos] = val;
        if (val != null) {
            this.values[pos].setParent(this);
        }
    }
    
    public Expression expAtPosition(int pos) {
        return this.values[pos];
    }
    
    /**
     * Returns the number of elements in this ArrayInitializerExpression.
     * 
     * @return
     */
    public int getArrayElementCount() {
        return this.values.length;
    }
    
    @Override
    public String toString() {
        String ret = super.toString() + "{";
        for (Expression exp : this.values) {
            ret = ret + exp.toString().replace(";", "") + ", ";
        }
        if (this.values.length > 0) {
            ret = ret.substring(0, ret.length() - 2);
        }
        ret = ret + "}";
        return ret + ";";
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        for (Expression exp : this.values) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.addAll(Arrays.asList(this.values));
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return this.values[index];
    }
    
    @Override
    public int getNumOfChildren() {
        return this.values.length;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        replaceExpressionArray(this.values, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(this.values);
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArrayInitializerExpression other = (ArrayInitializerExpression) obj;
        if (!Arrays.equals(this.values, other.values)) {
            return false;
        }
        return true;
    }
    
}
