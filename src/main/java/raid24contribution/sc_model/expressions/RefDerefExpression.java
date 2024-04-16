package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * This expression represents all referencing and dereferencing expressions like referencing of
 * variables (&x) and dereferencing of pointers (*ptr).
 * 
 */
public class RefDerefExpression extends Expression {
    
    private static final long serialVersionUID = 2397706482279356874L;
    
    /**
     * The referencing operation (&)
     */
    public static final boolean REFERENCING = true;
    /**
     * The dereferencing operation (*)
     */
    public static final boolean DEREFERENCING = false;
    
    private Expression expression;
    private boolean isReferencing;
    
    public RefDerefExpression(Node n, Expression exp, boolean isReferencing) {
        super(n);
        setExpression(exp);
        this.isReferencing = isReferencing;
    }
    
    public Expression getExpression() {
        return this.expression;
    }
    
    public void setExpression(Expression exp) {
        this.expression = exp;
        this.expression.setParent(this);
    }
    
    public boolean isReferencing() {
        return this.isReferencing;
    }
    
    public boolean isDerefencing() {
        return !isReferencing();
    }
    
    @Override
    public String toString() {
        return super.toString() + (this.isReferencing ? "&" : "*") + this.expression.toString();
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.expression);
        exps.addAll(this.expression.getInnerExpressions());
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.expression);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.expression;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.expression = replaceSingleExpression(this.expression, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.expression == null) ? 0 : this.expression.hashCode());
        result = prime * result + (this.isReferencing ? 1231 : 1237);
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
        RefDerefExpression other = (RefDerefExpression) obj;
        if (this.expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!this.expression.equals(other.expression)) {
            return false;
        }
        if (this.isReferencing != other.isReferencing) {
            return false;
        }
        return true;
    }
}
