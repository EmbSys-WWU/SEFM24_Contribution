package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression represents a switch-case-construct. It contains an expression for the initial
 * switch-statement and one condition and one body per case statement.
 * 
 * 
 */

public class SwitchExpression extends Expression {
    
    private static transient final Logger logger = LogManager.getLogger(SwitchExpression.class.getName());
    
    private static final long serialVersionUID = -3201626133965710498L;
    private Expression switchExpression;
    
    /**
     * Contains the bodies of all cases, ordered by appearance.
     */
    private List<Expression> cases;
    
    public SwitchExpression(Node n, Expression switchExp) {
        super(n);
        setSwitchExpression(switchExp);
        this.cases = new LinkedList<>();
    }
    
    /**
     * Adds a case, consisting of condition and a body to the end of the switch expression.
     * 
     * @param condition - condition for the switch
     * @param body - body of the case
     */
    public void addCase(CaseExpression ce) {
        ce.setParent(this);
        this.cases.add(ce);
    }
    
    public List<Expression> getCases() {
        return this.cases;
    }
    
    public void setCases(List<Expression> ces) {
        this.cases = ces;
        for (Expression exp : this.cases) {
            exp.setParent(this);
        }
    }
    
    @Override
    public String toString() {
        String ret = super.toString();
        ret = "switch(" + this.switchExpression.toString().replace(";", "") + ") {\n";
        
        for (Expression ce : this.cases) {
            ret += ce.toString();
        }
        ret = ret + "}\n";
        
        return ret;
    }
    
    public Expression getSwitchExpression() {
        return this.switchExpression;
    }
    
    public void setSwitchExpression(Expression switchExpression) {
        this.switchExpression = switchExpression;
        this.switchExpression.setParent(this);
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.switchExpression);
        exps.addAll(this.switchExpression.getInnerExpressions());
        
        for (Expression ce : this.cases) {
            if (ce instanceof CaseExpression cexp) {
                if (!cexp.isDefaultCase()) {
                    exps.add(cexp.getCondition());
                }
                
                for (Expression exp : cexp.getBody()) {
                    exps.add(exp);
                    exps.addAll(exp.getInnerExpressions());
                }
            } else {
                logger.error("Encounterd an expression in cases which is not a CaseExpression but {}.",
                        ce.getClass().getName());
            }
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.switchExpression);
        ret.addAll(this.cases);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.switchExpression;
            default -> this.cases.get(index - 1);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return this.cases.size() + 1;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.switchExpression = replaceSingleExpression(this.switchExpression, replacements);
        replaceExpressionList(this.cases, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.cases == null) ? 0 : this.cases.hashCode());
        result = prime * result + ((this.switchExpression == null) ? 0 : this.switchExpression.hashCode());
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
        if (!(obj instanceof SwitchExpression other)) {
            return false;
        }
        if (this.cases == null) {
            if (other.cases != null) {
                return false;
            }
        } else if (!this.cases.equals(other.cases)) {
            return false;
        }
        if (this.switchExpression == null) {
            if (other.switchExpression != null) {
                return false;
            }
        } else if (!this.switchExpression.equals(other.switchExpression)) {
            return false;
        }
        return true;
    }
    
}
