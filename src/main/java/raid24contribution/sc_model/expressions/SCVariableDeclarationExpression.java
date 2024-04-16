package raid24contribution.sc_model.expressions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this Expression represents a Variable-Declaration inside of a function. it contains a Expression
 * which refer to the Variable and an expression which represent the initial value
 * 
 */
public class SCVariableDeclarationExpression extends Expression {
    
    private static final long serialVersionUID = -8499943635883952860L;
    private static final transient Logger logger =
            LogManager.getLogger(SCVariableDeclarationExpression.class.getName());
    
    private Expression variable;
    private List<Expression> initialValues;
    
    public SCVariableDeclarationExpression(Node n, Expression v, List<Expression> ini) {
        this(n, v);
        setInitialValues(ini);
    }
    
    public SCVariableDeclarationExpression(Node n, Expression v, Expression firstInit) {
        this(n, v);
        setFirstInitialValue(firstInit);
        
    }
    
    public SCVariableDeclarationExpression(Node n, Expression v) {
        super(n);
        this.initialValues = new LinkedList<>();
        setVariable(v);
        this.initialValues = new LinkedList<>();
    }
    
    public Expression getVariable() {
        return this.variable;
    }
    
    public void setVariable(Expression variable) {
        this.variable = variable;
        if (variable != null) {
            this.variable.setParent(this);
        }
        if (variable instanceof SCVariableExpression) {
            ((SCVariableExpression) variable).getVar().setDeclaration(this);
        } else if (variable instanceof SCClassInstanceExpression) {
            ((SCClassInstanceExpression) variable).getInstance().setDeclaration(this);
        }
    }
    
    public Expression getFirstInitialValue() {
        if (this.initialValues == null) {
            logger.error("initialValues is null, should be empty list");
            return null;
        } else if (!this.initialValues.isEmpty()) {
            return this.initialValues.get(0);
        } else {
            return null;
        }
    }
    
    public void setFirstInitialValue(Expression val) {
        if (val != null) {
            val.setParent(this);
            this.initialValues.add(val);
        }
    }
    
    public List<Expression> getInitialValues() {
        // defensive copying
        return new ArrayList<>(this.initialValues);
    }
    
    public void setInitialValues(List<Expression> initialValues) {
        if (initialValues == null) {
            logger.warn("trying to set null as initialValues");
            return;
        }
        this.initialValues = initialValues;
        for (Expression exp : this.initialValues) {
            exp.setParent(this);
        }
    }
    
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer(super.toString().replace(":", ":;"));
        if (this.variable instanceof SCVariableExpression v) {
            ret.append(v.getVar().getDeclarationString().replace(";", ""));
            ret.append(v.getVar().getInitializationString());
        } else if (this.variable instanceof SCClassInstanceExpression m) {
            ret.append(m.getInstance().getDeclarationString().replace(";", ""));
            ret.append(m.getInstance().getInitializationString());
        }
        
        ret.append(";");
        return ret.toString();
        
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        exps.add(this.variable);
        exps.addAll(this.variable.getInnerExpressions());
        exps.addAll(this.initialValues);
        for (Expression exp : this.initialValues) {
            exps.addAll(exp.getInnerExpressions());
        }
        
        return exps;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.variable);
        ret.addAll(this.initialValues);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.variable;
            default -> this.initialValues.get(index - 1);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return this.initialValues.size() + 1;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.variable = replaceSingleExpression(this.variable, replacements);
        replaceExpressionList(this.initialValues, replacements);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.initialValues == null) ? 0 : this.initialValues.hashCode());
        result = prime * result + ((this.variable == null) ? 0 : this.variable.hashCode());
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
        SCVariableDeclarationExpression other = (SCVariableDeclarationExpression) obj;
        if (this.initialValues == null) {
            if (other.initialValues != null) {
                return false;
            }
        } else if (!this.initialValues.equals(other.initialValues)) {
            return false;
        }
        if (this.variable == null) {
            if (other.variable != null) {
                return false;
            }
        } else if (!this.variable.toString().equals(other.variable.toString())) {
            return false;
        }
        return true;
    }
    
}
