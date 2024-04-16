package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCPort;

public class MultiSocketAccessExpression extends Expression {
    
    private static final long serialVersionUID = 1882082603088570197L;
    private SCPort portSocket;
    private List<Expression> access;
    
    public MultiSocketAccessExpression(Node n, SCPort ps, List<Expression> a) {
        super(n);
        this.portSocket = ps;
        setAccess(a);
    }
    
    @Override
    public String toString() {
        String ret = super.toString() + this.portSocket.getName();
        for (Expression e : this.access) {
            ret = ret + "[" + e.toString().replace(";", "") + "]";
        }
        
        return ret + ";";
    }
    
    public SCPort getPortSocket() {
        return this.portSocket;
    }
    
    public void setPortSocket(SCPort portSocket) {
        this.portSocket = portSocket;
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
        LinkedList<Expression> ret = new LinkedList<>();
        ret.addAll(this.access);
        return ret;
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
        MultiSocketAccessExpression other = (MultiSocketAccessExpression) obj;
        if (this.access == null) {
            if (other.access != null) {
                return false;
            }
        } else if (!this.access.equals(other.access)) {
            return false;
        }
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
