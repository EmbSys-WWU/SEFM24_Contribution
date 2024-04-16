package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.variables.SCClassInstance;

/**
 * this expression is used, if a ModuleInstance was addressed in the systemC-Code it only contains a
 * reference to the addressed ModuleInstance
 * 
 */
public class SCClassInstanceExpression extends Expression {
    
    private static final long serialVersionUID = 7335841989647544441L;
    
    protected SCClassInstance instance = null;
    
    public SCClassInstanceExpression(Node n, SCClassInstance m) {
        super(n);
        this.instance = m;
    }
    
    public SCClassInstance getInstance() {
        return this.instance;
    }
    
    @Override
    public String toString() {
        return super.toString() + this.instance.getName() + ";";
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
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {}
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.instance == null) ? 0 : this.instance.hashCode());
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
        SCClassInstanceExpression other = (SCClassInstanceExpression) obj;
        if (this.instance == null) {
            if (other.instance != null) {
                return false;
            }
        } else if (!this.instance.equals(other.instance)) {
            return false;
        }
        return true;
    }
}
