package raid24contribution.sc_model.expressions;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Represents the while loop.
 * 
 */
public class WhileLoopExpression extends LoopExpression {
    
    private static final long serialVersionUID = -3973267367960454253L;
    private static final transient Logger logger = LogManager.getLogger(WhileLoopExpression.class.getName());
    
    public WhileLoopExpression(Node n, String l, Expression cond, List<Expression> body) {
        super(n, l, cond, body);
    }
    
    public WhileLoopExpression(Node n, String l, Expression cond, List<Expression> body, int maxCount) {
        super(n, l, cond, body, maxCount);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof WhileLoopExpression && super.equals(obj);
    }
    
    @Override
    public String toString() {
        String ret = super.toString() + "while (" + getCondition().toString().replace(";", "") + ") {";
        List<Expression> loopBody = getLoopBody();
        for (Expression e : loopBody) {
            if (e == null) {
                logger.error("expression in loop body is null: {}", loopBody);
            } else {
                ret = ret + "\n" + e.toString()/* .replace(";", "") + ";" */;
            }
        }
        return ret + "\n}";
    }
}
