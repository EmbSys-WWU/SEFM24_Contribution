package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCVariable;

/**
 * this expression is used, if a variable was addressed in the SystemC-Code it contains a reference
 * to the adressed variable
 * 
 * 
 */
public class SCVariableExpression extends Expression {
    
    private static final long serialVersionUID = 3878014191163474264L;
    protected SCVariable var;
    
    public SCVariableExpression(Node n, SCVariable v) {
        super(n);
        this.var = v;
    }
    
    public SCVariable getVar() {
        return this.var;
    }
    
    public void setVar(SCVariable var) {
        this.var = var;
    }
    
    @Override
    public String toString() {
        return super.toString() + this.var.getName() + ";";
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
        SCVariableExpression other = (SCVariableExpression) obj;
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
