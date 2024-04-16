package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.LoopExpression;
import raid24contribution.sc_model.expressions.WhileLoopExpression;

/**
 * first we handle the first real childnode, this is the condition afterwards we handle the
 * block-child, which is the loop-body. Then we build a new expression and add it to the stack.
 * 
 * The user may annotate the maximum iteration frequency for timing analysis. The annotation has to
 * be the first comment inside the loop containing the fixed string "// MAX_ITERATIONS = X " (case
 * and space sensitive), where X is an positive integer value. If there is a loop annotation inside
 * the block we store the value.
 * 
 */
public class WhileStatementTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        handleNode(findFirstRealChild(node), e);
        
        Expression condition = e.getExpressionStack().pop();
        
        List<Node> blocks = findChildNodes(node, "block");
        int size = e.getExpressionStack().size();
        
        handleNode(blocks.get(0), e);
        
        int maxIterations = -1; // -1 means unknown
        // handle simple infinite loop
        if (condition instanceof ConstantExpression constCond) {
            if (constCond.getValue() == "1" || constCond.getValue().equals("true")) {
                maxIterations = 0;
            }
        }
        // but overwrite if annotated max iterations
        Node comment = findChildNode(blocks.get(0), "comment");
        if (comment != null) {
            Node nameNode = comment.getAttributes().getNamedItem("name");
            String name = nameNode.getNodeValue();
            if (name.startsWith("// MAX_ITERATIONS = ")) {
                maxIterations = Integer.valueOf(name.substring("// MAX_ITERATIONS = ".length()));
            }
        }
        
        
        List<Expression> body = new ArrayList<>();
        // put the Expressions in the right order in the then-block
        for (int i = size; i < e.getExpressionStack().size(); i++) {
            body.add(e.getExpressionStack().get(i));
        }
        // remove these Expressions from the stack
        while (e.getExpressionStack().size() > size) {
            e.getExpressionStack().pop();
        }
        
        LoopExpression le = new WhileLoopExpression(node, "", condition, body, maxIterations);
        e.getExpressionStack().add(le);
    }
}
