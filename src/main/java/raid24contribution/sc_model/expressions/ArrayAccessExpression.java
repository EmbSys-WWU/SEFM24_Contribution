package raid24contribution.sc_model.expressions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCVariable;

/**
 * This Expression represents the access to an array (e.g., arr[x]). An array can either be a
 * SCArray or a SCPointer. It contains the array (e.g. the scvar from SCVarExpr) and the expression
 * used to access the array.
 * 
 * 
 */
public class ArrayAccessExpression extends SCVariableExpression {
    
    private static final long serialVersionUID = -5917543828733388700L;
    
    /**
     * An array can either be represented by a SCArray or by a SCPointer.
     */
    protected List<Expression> access;
    
    public ArrayAccessExpression(Node n, SCVariable array, List<Expression> access) {
        super(n, array);
        setAccess(access);
    }
    
    public ArrayAccessExpression(Node n, SCVariable array, Expression accessExpr) {
        this(n, array, new ArrayList<>());
        this.access.add(accessExpr);
    }
    
    @Override
    public String toString() {
        String ret = !this.label.equals("") ? this.label + ": " + this.var.getName() : this.var.getName();
        for (Expression e : this.access) {
            ret = ret + "[" + e.toStringNoSem() + "]";
        }
        return ret + ";";
    }
    
    public List<Expression> getAccess() {
        return this.access;
    }
    
    public void setAccess(List<Expression> access) {
        this.access = access;
        for (Expression exp : this.access) {
            exp.setParent(this);
        }
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        for (Expression exp : this.access) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        return new LinkedList<>(this.access);
    }
    
    @Override
    public Expression getChild(int index) {
        return this.access.get(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return this.access.size();
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        replaceExpressionList(this.access, replacements);
        
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.access == null) ? 0 : this.access.hashCode());
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
        ArrayAccessExpression other = (ArrayAccessExpression) obj;
        if (this.access == null) {
            if (other.access != null) {
                return false;
            }
        } else if (!this.access.equals(other.access)) {
            return false;
        }
        return true;
    }
    
}
