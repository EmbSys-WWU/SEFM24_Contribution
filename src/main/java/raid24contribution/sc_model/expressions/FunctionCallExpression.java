package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCFunction;

/**
 * this expression represent a FunctionCall it contains a reference to the function and a list of
 * Expressions which represent the Call-Parameter
 * 
 * 
 */
public class FunctionCallExpression extends Expression {
    
    private static final long serialVersionUID = -6937911626522812L;
    private SCFunction function;
    private List<Expression> parameters;
    
    public FunctionCallExpression(Node n, SCFunction function, List<Expression> params) {
        super(n);
        // this.function = function;
        setFunction(function);
        setParameters(params);
        
    }
    
    public void setFunction(SCFunction function) {
        this.function = function;
    }
    
    public SCFunction getFunction() {
        return this.function;
    }
    
    public void setParameters(List<Expression> params) {
        if (params == null) {
            params = new LinkedList<>();
        }
        this.parameters = params;
        for (Expression exp : this.parameters) {
            exp.setParent(this);
        }
        
    }
    
    public void addParameters(List<Expression> params) {
        for (Expression exp : params) {
            addSingleParameter(exp);
        }
    }
    
    public void addSingleParameter(Expression param) {
        param.setParent(this);
        this.parameters.add(param);
    }
    
    public List<Expression> getParameters() {
        return this.parameters;
    }
    
    @Override
    public String toString() {
        String ret = super.toString() + this.function.getName() + "(";
        if (this.parameters != null) {
            for (Expression e : this.parameters) {
                ret = ret + e.toString().replace(";", "") + ", ";
            }
            if (this.parameters.size() > 0) {
                ret = ret.substring(0, ret.length() - 2);
            }
        }
        ret = ret + ");";
        return ret;
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        for (Expression exp : this.parameters) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.addAll(this.parameters);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return this.parameters.get(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return this.parameters.size();
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        replaceExpressionList(this.parameters, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.function == null) ? 0 : this.function.toString().hashCode());
        result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
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
        FunctionCallExpression other = (FunctionCallExpression) obj;
        if (this.function == null) {
            if (other.function != null) {
                return false;
            }
        } else if (!this.function.toString().equals(other.function.toString())) {
            return false;
        }
        if (this.parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!this.parameters.equals(other.parameters)) {
            return false;
        }
        return true;
    }
}
