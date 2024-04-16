package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * This class represents the C++ output cout. All elements of a statement "cout << x << y << z;" are
 * hold in the expressions-List.
 * 
 */
public class OutputExpression extends Expression {
    
    private static final long serialVersionUID = -1804029659922367022L;
    private LinkedList<Expression> expressions;
    
    public OutputExpression(Node n, String label, LinkedList<Expression> expressions) {
        super(n, label);
        setExpressions(expressions);
    }
    
    @Override
    public String toString() {
        String out = "cout";
        for (Expression exp : this.expressions) {
            out += " << " + exp.toString().replaceAll(";", "");
        }
        out += ";";
        return out;
    }
    
    public LinkedList<Expression> getExpressions() {
        return this.expressions;
    }
    
    public void setExpressions(LinkedList<Expression> expressions) {
        this.expressions = expressions;
        for (Expression exp : this.expressions) {
            exp.setParent(this);
        }
    }
    
    /**
     * Adds the expression exp to the end of the expressionlist.
     * 
     * @param exp expression to add.
     */
    public void addExpression(Expression exp) {
        this.expressions.add(exp);
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        
        for (Expression exp : this.expressions) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.addAll(this.expressions);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return this.expressions.get(index);
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        replaceExpressionList(this.expressions, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.expressions == null) ? 0 : this.expressions.hashCode());
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
        OutputExpression other = (OutputExpression) obj;
        if (this.expressions == null) {
            if (other.expressions != null) {
                return false;
            }
        } else if (!this.expressions.equals(other.expressions)) {
            return false;
        }
        return true;
    }
    
}
