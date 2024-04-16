package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression represents the If-Then-Else-Construct it contains one Expression which represent
 * the condition anf two lists of Expressions, one represents the then-case and one the else-case
 * 
 * 
 */
public class IfElseExpression extends Expression {
    
    private static final long serialVersionUID = -5645500451034258989L;
    private Expression condition;
    private List<Expression> thenBlock;
    private List<Expression> elseBlock;
    
    public IfElseExpression(Node n, Expression cond, List<Expression> t, List<Expression> e) {
        super(n);
        setCondition(cond);
        setThenBlock(t);
        setElseBlock(e);
    }
    
    public IfElseExpression(Node n, Expression cond, Expression t, Expression e) {
        super(n);
        setCondition(cond);
        addThenExpression(t);
        addElseExpression(e);
    }
    
    @Override
    public String toString() {
        String ret = super.toString() + "if (" + this.condition.toString().replace(";", "") + ") {";
        for (Expression e : this.thenBlock) {
            ret += "\n" + e.toString()/* .replace(";", "") + ";" */;
        }
        if (!this.elseBlock.isEmpty()) {
            ret += "\n} else {";
            for (Expression e : this.elseBlock) {
                ret += "\n" + e.toString()/* .replace(";", "") + ";" */;
            }
            
        }
        return ret + "\n}";
    }
    
    public void addThenExpression(Expression exp) {
        if (this.thenBlock == null) {
            this.thenBlock = new LinkedList<>();
        }
        exp.setParent(this);
        this.thenBlock.add(exp);
        
    }
    
    public void addElseExpression(Expression exp) {
        if (this.elseBlock == null) {
            this.elseBlock = new LinkedList<>();
        }
        exp.setParent(this);
        this.elseBlock.add(exp);
    }
    
    public List<Expression> getThenBlock() {
        return this.thenBlock;
    }
    
    public void setThenBlock(List<Expression> thenBlock) {
        this.thenBlock = thenBlock;
        for (Expression exp : this.thenBlock) {
            exp.setParent(this);
        }
    }
    
    public List<Expression> getElseBlock() {
        return this.elseBlock;
    }
    
    public void setElseBlock(List<Expression> elseBlock) {
        this.elseBlock = elseBlock;
        for (Expression exp : this.elseBlock) {
            exp.setParent(this);
        }
    }
    
    public Expression getCondition() {
        return this.condition;
    }
    
    public void setCondition(Expression cond) {
        this.condition = cond;
        this.condition.setParent(this);
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.condition);
        exps.addAll(this.condition.getInnerExpressions());
        
        for (Expression exp : this.thenBlock) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        
        for (Expression exp : this.elseBlock) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.condition);
        ret.addAll(this.thenBlock);
        ret.addAll(this.elseBlock);
        return ret;
    }
    
    @Override
    public int getNumOfChildren() {
        return this.thenBlock.size() + this.elseBlock.size() + 1;
    }
    
    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return this.condition;
        }
        index--;
        if (index < this.thenBlock.size()) {
            return this.thenBlock.get(index);
        }
        index -= this.thenBlock.size();
        return this.elseBlock.get(index);
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        
        this.condition = replaceSingleExpression(this.condition, replacements);
        replaceExpressionList(this.thenBlock, replacements);
        replaceExpressionList(this.elseBlock, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.condition == null) ? 0 : this.condition.hashCode());
        result = prime * result + ((this.elseBlock == null) ? 0 : this.elseBlock.hashCode());
        result = prime * result + ((this.thenBlock == null) ? 0 : this.thenBlock.hashCode());
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
        IfElseExpression other = (IfElseExpression) obj;
        if (this.condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!this.condition.equals(other.condition)) {
            return false;
        }
        if (this.elseBlock == null) {
            if (other.elseBlock != null) {
                return false;
            }
        } else if (!this.elseBlock.equals(other.elseBlock)) {
            return false;
        }
        if (this.thenBlock == null) {
            if (other.thenBlock != null) {
                return false;
            }
        } else if (!this.thenBlock.equals(other.thenBlock)) {
            return false;
        }
        return true;
    }
}
