package raid24contribution.sc_model.expressions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * Represents a 'new $type' or 'new $type(init)' expression.
 *
 */
public class NewExpression extends Expression {
    
    private static Logger logger = LogManager.getLogger(NewExpression.class.getName());
    
    public NewExpression(Node n) {
        super(n);
        this.arguments = new ArrayList<>(0);
    }
    
    private static final long serialVersionUID = -1605241181529469122L;
    private String objType;
    private List<Expression> arguments;
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        return new LinkedList<>(this.arguments);
    }
    
    @Override
    public Expression getChild(int index) {
        return this.arguments.get(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return this.arguments.size();
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        replaceExpressionList(this.arguments, replacements);
    }
    
    @Override
    public String toString() {
        StringBuffer out = new StringBuffer("new " + this.objType);
        if (this.arguments.size() > 0) {
            String sep = ", ";
            out.append("(");
            for (Expression e : this.arguments) {
                out.append(e.toString().replace(";", "") + sep);
            }
            out.setLength(out.length() - sep.length());
            out.append(")");
        }
        // out.append(";"); // right?
        return out.toString();
    }
    
    /**
     * @return the objType
     */
    public String getObjType() {
        return this.objType;
    }
    
    /**
     * @param objType the objType to set
     */
    public void setObjType(String objType) {
        this.objType = objType;
    }
    
    /**
     * @return the arguments
     */
    public List<Expression> getArguments() {
        return this.arguments;
    }
    
    /**
     * @param arguments the arguments to set
     */
    public void setArguments(List<Expression> arguments) {
        for (Expression arg : arguments) {
            arg.setParent(this);
        }
        this.arguments = arguments;
    }
    
}
