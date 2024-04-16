package raid24contribution.engine.nodetransformer;

import java.util.LinkedList;
import java.util.Stack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.OutputExpression;

/**
 * Transforms outputstream-nodes. These nodes are generated out of cout-Statements in SystemC. We
 * generate an OutputExpression from these statements.
 * 
 */
public class OutputStreamTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(OutputStreamTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        String access = NodeUtil.getAttributeValueByName(node, "name");
        if (access.equals("cout")) {
            Stack<Expression> oldExpressionStack = e.getExpressionStack();
            Stack<Expression> tempStack = new Stack<>();
            e.setExpressionStack(tempStack);
            
            LinkedList<Expression> exps = new LinkedList<>();
            handleChildNodes(node, e);
            while (!e.getExpressionStack().isEmpty()) {
                exps.addFirst(e.getExpressionStack().pop());
            }
            
            OutputExpression oe = new OutputExpression(node, "", exps);
            
            oldExpressionStack.add(oe);
            e.setExpressionStack(oldExpressionStack);
        } else {
            // we have an outputstream we can not handle
            logger.error("{}: Encountered an outputstream node with name {} which cannot be handled (yet).",
                    NodeUtil.getFixedAttributes(node), access);
        }
    }
}
