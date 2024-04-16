package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression represents a Binary-Construct out of the systemC-Code it contains the left-hand
 * expression, the operator and the right-hand expression a BinaryExpressions can be everything
 * where to exressions ar conected their are, Arithmetic-Operators, Logic-Operators,
 * relational-Operators, but also the "." for PostFix-Constructs and the "" All Operators are in the
 * <link>OPERATOR</link>-Class
 * 
 * 
 */
public class BinaryExpression extends Expression {
    
    private static final long serialVersionUID = -2396981160240523208L;
    private Expression left;
    private String op;
    private Expression right;
    
    public BinaryExpression(Node n, Expression left, String o, Expression right) {
        super(n);
        setLeft(left);
        this.op = o;
        setRight(right);
    }
    
    @Override
    public String toString() {
        return super.toString() + this.left.toString().replace(";", "") + " " + this.op + " "
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
    public LinkedList<Expression> crawlDeeper() {
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
        BinaryExpression other = (BinaryExpression) obj;
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
}
