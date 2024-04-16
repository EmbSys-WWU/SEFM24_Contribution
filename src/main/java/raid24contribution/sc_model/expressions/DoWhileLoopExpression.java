package raid24contribution.sc_model.expressions;

import java.util.List;
import org.w3c.dom.Node;

/**
 * Represents the do-while loop.
 *
 */
public class DoWhileLoopExpression extends LoopExpression {
    
    private static final long serialVersionUID = -1590239915068409811L;
    
    public DoWhileLoopExpression(Node n, String l, Expression cond, List<Expression> body) {
        super(n, l, cond, body);
    }
    
    @Override
    public String toString() {
        String ret = super.toString() + "do {";
        // for (Expression e : getLoopBody()) {
        // System.out.println("Loop : " + e);
        // ret = ret + "\n" + e.toString().replace(";", "") + ";";
        // }
        // @Kannan : Added a fix for missing semicolon in case of complex expressions inside loop
        String ret1 = getLoopBody().toString().replace("[", "");
        String ret2 = ret1.replace(";,", ";");
        String ret3 = ret2.replace("},", "}");
        String ret4 = ret3.replace("]", "");
        ret = ret + "\n" + ret4 + ";";
        // @Kannan : Ends here
        return ret + "\n} while (" + getCondition().toString().replace(";", "") + ");";
    }
}
