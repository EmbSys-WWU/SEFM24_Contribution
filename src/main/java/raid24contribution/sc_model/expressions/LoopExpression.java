package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this Expression represent the basic loop, containing a condition and a body. For a specific loop
 * please use the implementations like ForLoop, WhileLoop and DoWhileLoop.
 * 
 * 
 */
public abstract class LoopExpression extends Expression {
    
    private static final long serialVersionUID = -1775301091944386408L;
    private static final transient Logger logger = LogManager.getLogger(LoopExpression.class.getName());
    
    private Expression condition = null;
    private List<Expression> loopBody = null;
    
    /** User may annotate maximum loop iterations. -1 means unknown, 0 = infinity. */
    private int maxCount = -1;
    
    public LoopExpression(Node n, String l, Expression cond, List<Expression> body) {
        super(n, l);
        setCondition(cond);
        setLoopBody(body);
    }
    
    public LoopExpression(Node n, String l, Expression cond, List<Expression> body, int maxCount) {
        super(n, l);
        setCondition(cond);
        setLoopBody(body);
        setMaxCount(maxCount);
    }
    
    public Expression getCondition() {
        return this.condition;
    }
    
    
    public void setCondition(Expression condition) {
        this.condition = condition;
        this.condition.setParent(this);
    }
    
    public List<Expression> getLoopBody() {
        return this.loopBody;
    }
    
    public void setLoopBody(List<Expression> loopBody) {
        this.loopBody = loopBody;
        for (Expression exp : this.loopBody) {
            if (exp == null) {
                logger.error("expression in loop body is null: {}", loopBody);
            } else {
                exp.setParent(this);
            }
        }
    }
    
    /**
     * Get the max iteration frequency. -1 means unknown, 0 means infinite (e.g. while(1))
     *
     * @return The maximum frequency this loop is iterated.
     */
    public int getMaxCount() {
        return this.maxCount;
    }
    
    
    /**
     * Set the max iteration frequency. -1 means unknown, 0 means infinite (e.g. while(1))
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
    
    @Override
    public String toString() {
        if (this.label != null && !this.label.equals("")) {
            return this.label + ": ";
        } else {
            return "";
        }
    }
    
    public void addExpression(Expression exp) {
        exp.setParent(this);
        this.loopBody.add(exp);
    }
    
    /**
     * Return everything in between the '()' of the loop.
     *
     * @return
     */
    public List<Expression> getHeader() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.condition);
        return ret;
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.condition);
        exps.addAll(this.condition.getInnerExpressions());
        
        for (Expression exp : this.loopBody) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.condition);
        ret.addAll(this.loopBody);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.condition;
            default -> this.loopBody.get(index - 1);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return this.loopBody.size() + 1;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.condition = replaceSingleExpression(this.condition, replacements);
        replaceExpressionList(this.loopBody, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.condition == null) ? 0 : this.condition.hashCode());
        result = prime * result + ((this.loopBody == null) ? 0 : this.loopBody.hashCode());
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
        LoopExpression other = (LoopExpression) obj;
        if (this.condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!this.condition.equals(other.condition)) {
            return false;
        }
        if (this.loopBody == null) {
            if (other.loopBody != null) {
                return false;
            }
        } else if (!this.loopBody.equals(other.loopBody)) {
            return false;
        }
        return true;
    }
    
}
