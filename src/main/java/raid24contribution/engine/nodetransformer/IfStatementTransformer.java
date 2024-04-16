package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.IfElseExpression;

/**
 * first we handle the first real childnode, thats the condition afterwards we get all childnodes
 * named "block" we handle the first one, cause this is the then-case then we look if we have an
 * other block in the list, thats the else-case, and handle it then we build a new IfElseExpression
 * and puch it to the stack
 * 
 */
public class IfStatementTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        handleNode(findFirstRealChild(node), e);
        Expression condition = e.getExpressionStack().pop();
        
        List<Node> blocks = findChildNodes(node, "block");
        
        int size = e.getExpressionStack().size();
        
        handleNode(blocks.get(0), e);
        
        List<Expression> then = new ArrayList<>();
        // put the Expressions in the right order in the then-block
        for (int i = size; i < e.getExpressionStack().size(); i++) {
            then.add(e.getExpressionStack().get(i));
        }
        // remove these Expressions from the stack
        while (e.getExpressionStack().size() > size) {
            e.getExpressionStack().pop();
        }
        List<Expression> Else = new ArrayList<>();
        if (blocks.size() == 2) {
            handleNode(blocks.get(1), e);
            
            Else = new ArrayList<>();
            // put the Expressions in the right order in the then-block
            for (int i = size; i < e.getExpressionStack().size(); i++) {
                Else.add(e.getExpressionStack().get(i));
            }
            // remove these Expressions from the stack
            while (e.getExpressionStack().size() > size) {
                e.getExpressionStack().pop();
            }
            
        } else {
            // big problem impossible
        }
        
        IfElseExpression iee = new IfElseExpression(node, condition, then, Else);
        e.getExpressionStack().push(iee);
        
    }
}
