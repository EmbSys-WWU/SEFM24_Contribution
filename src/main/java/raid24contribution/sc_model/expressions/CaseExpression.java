package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * Represents a single case in a switch case statement.
 * 
 * 
 */
public class CaseExpression extends Expression {
    
    private static final long serialVersionUID = 7916149870446969571L;
    
    private boolean isDefaultCase;
    private Expression condition;
    private List<Expression> body;
    
    public CaseExpression(Node n, Expression condition, List<Expression> body) {
        super(n);
        this.isDefaultCase = false;
        setCondition(condition);
        setBody(body);
    }
    
    /**
     * Constructor for a default case expression.
     * 
     * @param n
     * @param body
     */
    public CaseExpression(Node n, List<Expression> body) {
        this(n, null, body);
    }
    
    @Override
    public String toString() {
        String ret = "";
        if (this.isDefaultCase) {
            ret += "default: \n";
        } else {
            ret += "case " + this.condition + ": \n";
        }
        for (Expression exp : this.body) {
            ret += exp + "\n";
        }
        return ret;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        if (!this.isDefaultCase) {
            ret.add(this.condition);
        }
        ret.addAll(this.body);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        if (index == 0 && !this.isDefaultCase) {
            return this.condition;
        }
        index = this.isDefaultCase ? index : index - 1;
        return this.body.get(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return this.body.size() + (this.isDefaultCase ? 0 : 1);
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        
        if (!this.isDefaultCase) {
            this.condition = replaceSingleExpression(this.condition, replacements);
        }
        
        replaceExpressionList(this.body, replacements);
        
    }
    
    public boolean isDefaultCase() {
        return this.isDefaultCase;
    }
    
    public Expression getCondition() {
        return this.condition;
    }
    
    public void setCondition(Expression condition) {
        this.condition = condition;
        if (condition != null) {
            this.condition.setParent(this);
            this.isDefaultCase = false;
        } else {
            this.isDefaultCase = true;
        }
    }
    
    public List<Expression> getBody() {
        return this.body;
    }
    
    public void setBody(List<Expression> body) {
        this.body = body;
        for (Expression exp : body) {
            exp.setParent(this);
        }
    }
    
}
