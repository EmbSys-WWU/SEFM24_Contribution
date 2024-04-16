package raid24contribution.sc_model.expressions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * this expression represents the Questionmark-Construct it contains one expression as Condition and
 * two Expression which represent the if- and the else-branch
 * 
 * 
 */
public class QuestionmarkExpression extends IfElseExpression {
    
    private static Logger logger = LogManager.getLogger(QuestionmarkExpression.class.getName());
    
    private static final long serialVersionUID = -8688318767339929612L;
    
    public QuestionmarkExpression(Node n, Expression cond, Expression t, Expression e) {
        super(n, cond, t, e);
    }
    
    public Expression getThen() {
        if (!getThenBlock().isEmpty()) {
            return super.getThenBlock().get(0);
        } else {
            return null;
        }
    }
    
    public Expression getElse() {
        if (!getElseBlock().isEmpty()) {
            return super.getElseBlock().get(0);
        } else {
            return null;
        }
    }
    
    @Override
    public String toString() {
        String ret = "";
        String ifElseString = super.toString();
        
        if (ifElseString.indexOf("if") != 0) {
            // add label declaration to return string
            ret += ifElseString.substring(0, ifElseString.indexOf("if"));
        }
        
        ret += getCondition().toString().replace(";", "") + " ? " + getThen().toString().replace(";", "") + " : "
                + getElse().toString().replace(";", "");
        
        return ret + ";";
    }
    
    public IfElseExpression toIfElseExpression() {
        logger.warn("Converting conditional operator :? to if/else control structure. The result is probably wrong");
        return new IfElseExpression(getNode(), getCondition(), getThen(), getElse());
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof QuestionmarkExpression && super.equals(obj);
    }
    
}
