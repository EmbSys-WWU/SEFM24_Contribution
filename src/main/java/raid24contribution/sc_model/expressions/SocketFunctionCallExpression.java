package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression is used for a FunctionCall of a socket at the runtime of the transformation we
 * don't know to which ModuleInstance a Port is connected so we can't refer to a specified function,
 * we only can save the functionname.
 *
 */
public class SocketFunctionCallExpression extends Expression {
    
    private static final long serialVersionUID = -1657072658518322361L;
    private String functionName;
    
    public SocketFunctionCallExpression(Node n, String funName) {
        super(n);
        this.functionName = funName;
    }
    
    @Override
    public String toString() {
        return super.toString() + this.functionName + "();";
    }
    
    public String getFunction() {
        return this.functionName;
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
        result = prime * result + ((this.functionName == null) ? 0 : this.functionName.hashCode());
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
        SocketFunctionCallExpression other = (SocketFunctionCallExpression) obj;
        if (this.functionName == null) {
            if (other.functionName != null) {
                return false;
            }
        } else if (!this.functionName.equals(other.functionName)) {
            return false;
        }
        return true;
    }
}
