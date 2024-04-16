package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * This Expression is a container for a sequence of expressions.
 * 
 */
public class ExpressionBlock extends Expression {
    
    private static final long serialVersionUID = -3875422736888189260L;
    
    protected List<Expression> block;
    
    public ExpressionBlock(Node n, List<Expression> block) {
        super(n);
        setBlock(block);
    }
    
    public ExpressionBlock(Node n) {
        super(n);
        setBlock(null);
    }
    
    public List<Expression> getBlock() {
        return this.block;
    }
    
    public void setBlock(List<Expression> blk) {
        if (blk == null) {
            this.block = new LinkedList<>();
        } else {
            this.block = blk;
        }
    }
    
    public boolean addExpression(Expression exp) {
        if (exp == null) {
            return false;
        }
        if (this.block == null) {
            setBlock(null);
        }
        this.block.add(exp);
        exp.setParent(this);
        return true;
    }
    
    public boolean addAll(List<Expression> ls) {
        for (Expression expr : ls) {
            if (!addExpression(expr)) {
                return false;
            }
        }
        return true;
    }
    
    public void emptyBlock() {
        setBlock(null);
    }
    
    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        if (this.block != null) {
            for (Expression exp : this.block) {
                if (exp != null) {
                    out.append(exp.toString()).append("\n");
                }
            }
            out.deleteCharAt(out.length() - 1);
        }
        return out.toString();
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        LinkedList<Expression> ls = new LinkedList<>();
        for (Expression expr : this.block) {
            ls.add(expr);
            ls.addAll(expr.getInnerExpressions());
        }
        return ls;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ls = new LinkedList<>();
        ls.addAll(this.block);
        return ls;
    }
    
    @Override
    public Expression getChild(int index) {
        return this.block.get(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return this.block.size();
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        replaceExpressionList(this.block, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * this.block.hashCode();
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
        ExpressionBlock other = (ExpressionBlock) obj;
        if (this.block == null) {
            if (other.block != null) {
                return false;
            }
        } else if (!this.block.equals(other.block)) {
            return false;
        }
        return true;
    }
}
