package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;



/**
 * This class is used to signal, that the containig variable should be set over a non deterministic
 * choice by the Transformer.
 * 
 * 
 */
public class SCVariableNonDetSet extends Expression {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The variableEx containing the var we have to set to a non deterministic value
     */
    protected SCVariableExpression var;
    
    public SCVariableNonDetSet(Node n, SCVariableExpression v) {
        super(n);
        this.var = v;
    }
    
    /**
     * @return the varEx containing the var that has to be set to a non deterministic value
     */
    public SCVariableExpression getVar() {
        return this.var;
    }
    
    
    @Override
    public String toString() {
        return super.toString() + " non deterministic " + this.var.toString();
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return 0;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {}
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.var == null) ? 0 : this.var.hashCode());
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
        SCVariableNonDetSet other = (SCVariableNonDetSet) obj;
        if (this.var == null) {
            if (other.var != null) {
                return false;
            }
        } else if (!this.var.equals(other.var)) {
            return false;
        }
        return true;
    }
    
}
