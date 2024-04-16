package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * This expression represents all unary expressions (expressions with one Operator in front or at
 * the end). This is only an abstract class and is specified further by UnaryPreExpression and
 * UnaryPostExpression.
 * 
 */
public class UnaryExpression extends Expression {
    
    public static final boolean PRE = true;
    public static final boolean POST = false;
    
    private static final long serialVersionUID = -4161776944237991064L;
    private Expression expression;
    private String operator;
    private boolean prepost;
    
    public UnaryExpression(Node n, boolean prepost, String op, Expression exp) {
        super(n);
        this.prepost = prepost;
        this.operator = op;
        setExpression(exp);
    }
    
    @Override
    public String toString() {
        if (this.prepost == PRE) {
            if (this.operator.equals("return") && this.expression == null) {
                return super.toString() + this.operator + ";";
            } else {
                return super.toString() + this.operator + this.expression.toString().replace(";", "") + ";";
            }
        } else {
            return super.toString() + this.expression.toString().replace(";", "") + this.operator + ";";
        }
    }
    
    public Expression getExpression() {
        return this.expression;
    }
    
    public void setExpression(Expression expression) {
        this.expression = expression;
        this.expression.setParent(this);
    }
    
    public String getOperator() {
        return this.operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public boolean isPrepost() {
        return this.prepost;
    }
    
    public void setPrepost(boolean prepost) {
        this.prepost = prepost;
    }
    
    public static long getSerialversionuid() {
        return serialVersionUID;
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
        if (index == 0) {
            return this.expression;
        }
        throw new IndexOutOfBoundsException(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return 1;
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
        result = prime * result + ((this.operator == null) ? 0 : this.operator.hashCode());
        result = prime * result + (this.prepost ? 1231 : 1237);
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
        UnaryExpression other = (UnaryExpression) obj;
        if (this.expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!this.expression.equals(other.expression)) {
            return false;
        }
        if (this.operator == null) {
            if (other.operator != null) {
                return false;
            }
        } else if (!this.operator.equals(other.operator)) {
            return false;
        }
        if (this.prepost != other.prepost) {
            return false;
        }
        return true;
    }
}
