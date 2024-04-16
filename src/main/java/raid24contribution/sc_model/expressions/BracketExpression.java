package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this Expression is only for better reading after a print
 * 
 * 
 */
public class BracketExpression extends Expression {
    
    private static final long serialVersionUID = 5690296452803522658L;
    
    private Expression inBrackets;
    
    public BracketExpression(Node n, Expression exp) {
        super(n);
        setInBrackets(exp);
    }
    
    public Expression getInBrackets() {
        return this.inBrackets;
    }
    
    public void setInBrackets(Expression exp) {
        this.inBrackets = exp;
        this.inBrackets.setParent(this);
    }
    
    @Override
    public String toString() {
        return super.toString() + "(" + this.inBrackets.toString().replace(";", "") + ");";
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.inBrackets);
        exps.addAll(this.inBrackets.getInnerExpressions());
        
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.inBrackets);
        return ret;
    }
    
    @Override
    public int getNumOfChildren() {
        return 1;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.inBrackets;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.inBrackets = replaceSingleExpression(this.inBrackets, replacements);
        
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.inBrackets == null) ? 0 : this.inBrackets.hashCode());
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
        BracketExpression other = (BracketExpression) obj;
        if (this.inBrackets == null) {
            if (other.inBrackets != null) {
                return false;
            }
        } else if (!this.inBrackets.equals(other.inBrackets)) {
            return false;
        }
        return true;
    }
    
}
