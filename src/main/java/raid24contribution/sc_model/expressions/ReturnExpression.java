package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression represents the return-construct it contains an expression where the returned
 * Expression is saved.
 * 
 * 
 */
public class ReturnExpression extends Expression {
    
    private static transient final Logger logger = LogManager.getLogger(ReturnExpression.class.getName());
    
    private static final long serialVersionUID = -8348637675858553260L;
    private Expression returnStatement;
    
    public ReturnExpression(Node n, Expression returnStatement) {
        super(n);
        setReturnStatement(returnStatement);
    }
    
    @Override
    public String toString() {
        if (this.returnStatement != null) {
            return super.toString() + "return " + this.returnStatement.toString().replace(";", "") + ";";
        } else {
            return super.toString() + "return;";
        }
    }
    
    public Expression getReturnStatement() {
        return this.returnStatement;
    }
    
    public void setReturnStatement(Expression returnStatement) {
        this.returnStatement = returnStatement;
        if (returnStatement != null) {
            this.returnStatement.setParent(this);
        }
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        if (this.returnStatement != null) {
            exps.add(this.returnStatement);
            exps.addAll(this.returnStatement.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        if (this.returnStatement != null) {
            ret.add(this.returnStatement);
        }
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        if (index != 0 || this.returnStatement == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return this.returnStatement;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        if (!replacements.isEmpty() && this.returnStatement != null) {
            this.returnStatement = replaceSingleExpression(this.returnStatement, replacements);
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.returnStatement == null) ? 0 : this.returnStatement.hashCode());
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
        ReturnExpression other = (ReturnExpression) obj;
        if (this.returnStatement == null) {
            if (other.returnStatement != null) {
                return false;
            }
        } else if (!this.returnStatement.equals(other.returnStatement)) {
            return false;
        }
        return true;
    }
    
}
