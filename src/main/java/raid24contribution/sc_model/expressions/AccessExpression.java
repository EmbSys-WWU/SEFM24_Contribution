package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * Represents all accesses to fields and functions of variables (e.g., a.val, ptr->val, x.foo()). A
 * field of a sc class instance is never accessed from outside. Hence, an access expression always
 * refers to structs or classes.
 * 
 */
public class AccessExpression extends Expression {
    
    private static final long serialVersionUID = 275880666766641758L;
    
    private Expression left;
    private Expression right;
    private String op;
    
    public AccessExpression(Node n, Expression left, String op, Expression right) {
        super(n);
        setLeft(left);
        setRight(right);
        this.op = op;
    }
    
    @Override
    public List<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.left);
        ret.add(this.right);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.left;
            case 1 -> this.right;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return 2;
    }
    
    @Override
    public String toString() {
        return super.toString() + this.left.toString().replace(";", "") + this.op
                + this.right.toString().replace(";", "") + ";";
    }
    
    public Expression getLeft() {
        return this.left;
    }
    
    public void setLeft(Expression left) {
        this.left = left;
        this.left.setParent(this);
    }
    
    public String getOp() {
        return this.op;
    }
    
    public void setOp(String op) {
        this.op = op;
    }
    
    public Expression getRight() {
        return this.right;
    }
    
    public void setRight(Expression right) {
        this.right = right;
        this.right.setParent(this);
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.left);
        exps.addAll(this.left.getInnerExpressions());
        exps.add(this.right);
        exps.addAll(this.right.getInnerExpressions());
        
        return exps;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        
        this.left = replaceSingleExpression(this.left, replacements);
        this.right = replaceSingleExpression(this.right, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.left == null) ? 0 : this.left.hashCode());
        result = prime * result + ((this.op == null) ? 0 : this.op.hashCode());
        result = prime * result + ((this.right == null) ? 0 : this.right.hashCode());
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
        AccessExpression other = (AccessExpression) obj;
        if (this.left == null) {
            if (other.left != null) {
                return false;
            }
        } else if (!this.left.equals(other.left)) {
            return false;
        }
        if (this.op == null) {
            if (other.op != null) {
                return false;
            }
        } else if (!this.op.equals(other.op)) {
            return false;
        }
        if (this.right == null) {
            if (other.right != null) {
                return false;
            }
        } else if (!this.right.equals(other.right)) {
            return false;
        }
        return true;
    }
    
    /**
     * finds the type of the access b< crawling into the right expressions
     *
     * @return
     */
    public String findType() {
        if (getRight() instanceof SCVariableExpression) {
            return ((SCVariableExpression) getRight()).getVar().getType();
        } else if (getRight() instanceof AccessExpression) {
            return ((AccessExpression) getRight()).findType();
        } else if (getRight() instanceof FunctionCallExpression) {
            FunctionCallExpression fce = (FunctionCallExpression) getRight();
            return fce.getFunction().getReturnType();
        } else {
            return null;
        }
    }
    
}
