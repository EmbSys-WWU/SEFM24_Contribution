package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.DoWhileLoopExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.LoopExpression;

/**
 * we have a do-while-loop we extract the block which represents the loop-body then we search for
 * the condition to parse it and save it afterwards we handle the loop-body at the end we create a
 * new loopExpression and add it to the stack
 * 
 */
public class DoStatementTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        int s = e.getExpressionStack().size();
        // handle Do-Block
        handleNode(findChildNode(node, "block"), e);
        // handle condition
        handleNode(findFirstChildNot(node, "block"), e);
        Expression cond = e.getExpressionStack().pop();
        List<Expression> exp_list = new ArrayList<>();
        while (s != e.getExpressionStack().size()) {
            exp_list.add(e.getExpressionStack().get(s));
            e.getExpressionStack().remove(s);
        }
        
        LoopExpression le = new DoWhileLoopExpression(node, "", cond, exp_list);
        
        e.getExpressionStack().add(le);
    }
}
