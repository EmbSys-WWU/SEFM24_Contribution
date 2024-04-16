package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCPort;

/**
 * this Expression is used, if a Port or a Socket was addressed in the SystemC-Code it contains a
 * reference to the Port or Socket
 * 
 */
public class SCPortSCSocketExpression extends Expression {
    
    private static final long serialVersionUID = -5043084393659564625L;
    private SCPort portSocket;
    
    public SCPortSCSocketExpression(Node n, SCPort ps) {
        super(n);
        this.portSocket = ps;
    }
    
    public SCPort getSCPortSCSocket() {
        return this.portSocket;
    }
    
    @Override
    public String toString() {
        return super.toString() + this.portSocket.getName() + ";";
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
        result = prime * result + ((this.portSocket == null) ? 0 : this.portSocket.hashCode());
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
        SCPortSCSocketExpression other = (SCPortSCSocketExpression) obj;
        if (this.portSocket == null) {
            if (other.portSocket != null) {
                return false;
            }
        } else if (!this.portSocket.equals(other.portSocket)) {
            return false;
        }
        return true;
    }
    
}
