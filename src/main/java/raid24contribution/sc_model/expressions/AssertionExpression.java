package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * This expression models the basic assert() statement.
 * 
 * 
 */
public class AssertionExpression extends Expression {
    
    private static final long serialVersionUID = -2536174919926485707L;
    
    /**
     * Represents the condition of the assertion.
     */
    private Expression condition;
    
    public AssertionExpression(Node n, Expression cond) {
        super(n);
        setCondition(cond);
    }
    
    public Expression getCondition() {
        return this.condition;
    }
    
    public void setCondition(Expression cond) {
        this.condition = cond;
        if (cond != null) {
            cond.setParent(this);
        }
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> list = new LinkedList<>();
        if (this.condition != null) {
            list.add(this.condition);
        }
        return list;
    }
    
    @Override
    public Expression getChild(int index) {
        if (index != 0 || this.condition == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return this.condition;
    }
    
    @Override
    public LinkedList<Expression> getInnerExpressions() {
        LinkedList<Expression> exps = new LinkedList<>();
        if (this.condition != null) {
            exps.add(this.condition);
            exps.addAll(this.condition.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.condition = replaceSingleExpression(this.condition, replacements);
    }
    
    @Override
    public String toString() {
        String ret = super.toString() + "assert(" + this.condition.toString().replace(";", "") + ");";
        return ret;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.condition == null) ? 0 : this.condition.hashCode());
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
        if (!(obj instanceof AssertionExpression other)) {
            return false;
        }
        if (this.condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!this.condition.equals(other.condition)) {
            return false;
        }
        return true;
    }
    
}
