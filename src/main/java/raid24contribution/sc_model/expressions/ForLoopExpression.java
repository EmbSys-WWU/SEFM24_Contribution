package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression represents a For-Loop it contains 3 Expressions which represent the
 * initial-construct, the final-construct and the iterator-construct it also contains a List of
 * Expressions, which represent the Body
 * 
 */

public class ForLoopExpression extends LoopExpression {
    
    private static final long serialVersionUID = -5415940107730537267L;
    private Expression initializer;
    private Expression iterator;
    
    public ForLoopExpression(Node n, String l, Expression initializer, Expression condition, Expression iterator,
            List<Expression> body) {
        super(n, l, condition, body);
        setInitializer(initializer);
        setIterator(iterator);
    }
    
    public ForLoopExpression(Node n, String l, Expression initializer, Expression condition, Expression iterator,
            List<Expression> body, int maxCount) {
        super(n, l, condition, body, maxCount);
        setInitializer(initializer);
        setIterator(iterator);
    }
    
    @Override
    public String toString() {
        
        String ret = super.toString() + "for(" + this.initializer.toString().replace(";", "") + "; "
                + getCondition().toString().replace(";", "") + "; " + this.iterator.toString().replace(";", "") + "){";
        for (Expression e : getLoopBody()) {
            // System.out.println(e);
            ret = ret + "\n\t" + e.toString()/* .replace(";", "") + ";" */;
        }
        return ret + "\n}";
    }
    
    public Expression getInitializer() {
        return this.initializer;
    }
    
    public void setInitializer(Expression initializer) {
        this.initializer = initializer;
        this.initializer.setParent(this);
    }
    
    public Expression getIterator() {
        return this.iterator;
    }
    
    public void setIterator(Expression iterator) {
        this.iterator = iterator;
        this.iterator.setParent(this);
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.initializer);
        exps.addAll(this.initializer.getInnerExpressions());
        exps.add(this.iterator);
        exps.addAll(this.iterator.getInnerExpressions());
        
        exps.addAll(super.getInnerExpressions());
        
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = super.crawlDeeper();
        ret.add(this.initializer);
        ret.add(this.iterator);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return getCondition();
        }
        index--;
        int bodyLength = getLoopBody().size();
        if (index < bodyLength) {
            return getLoopBody().get(index);
        }
        index -= bodyLength;
        return switch (index) {
            case 0 -> this.initializer;
            case 1 -> this.iterator;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return super.getNumOfChildren() + 2;
    }
    
    @Override
    public List<Expression> getHeader() {
        List<Expression> ret = super.getHeader();
        ret.add(0, this.initializer); // add at the begin
        ret.add(this.iterator);
        return ret;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        super.replaceInnerExpressions(replacements);
        this.initializer = replaceSingleExpression(this.initializer, replacements);
        this.iterator = replaceSingleExpression(this.iterator, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.initializer == null) ? 0 : this.initializer.hashCode());
        result = prime * result + ((this.iterator == null) ? 0 : this.iterator.hashCode());
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
        ForLoopExpression other = (ForLoopExpression) obj;
        if (this.initializer == null) {
            if (other.initializer != null) {
                return false;
            }
        } else if (!this.initializer.equals(other.initializer)) {
            return false;
        }
        if (this.iterator == null) {
            if (other.iterator != null) {
                return false;
            }
        } else if (!this.iterator.equals(other.iterator)) {
            return false;
        }
        return true;
    }
}
