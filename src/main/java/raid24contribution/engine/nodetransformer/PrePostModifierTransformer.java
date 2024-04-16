package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.UnaryExpression;

/**
 * first we handle the childnode then we get the top-expression from the stack next step is, get the
 * real type of the node and create the right unary-expression and add it to the stack
 * 
 */
public class PrePostModifierTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(PrePostModifierTransformer.class.getName());
    
    public static final String PRE_DEC = "predecrement_node";
    public static final String PRE_INC = "preincrement_node";
    public static final String POST_INC = "postincrement_node";
    public static final String POST_DEC = "postdecrement_node";
    
    @Override
    public void transformNode(Node node, Environment e) {
        
        handleChildNodes(node, e);
        String op = node.getNodeName();
        
        Expression exp = e.getExpressionStack().pop();
        
        if (op.equals(PRE_DEC)) {
            UnaryExpression ue = new UnaryExpression(node, true, "--", exp);
            e.getExpressionStack().push(ue);
        } else if (op.equals(PRE_INC)) { // !
            UnaryExpression ue = new UnaryExpression(node, true, "++", exp);
            e.getExpressionStack().push(ue);
        } else if (op.equals(POST_DEC)) {
            UnaryExpression ue = new UnaryExpression(node, false, "--", exp);
            e.getExpressionStack().push(ue);
        } else if (op.equals(POST_INC)) {
            UnaryExpression ue = new UnaryExpression(node, false, "++", exp);
            e.getExpressionStack().push(ue);
        } else {
            logger.error(
                    "{}: Configuration Error: PrePostModifierTransformer should only be used for post/pre in/decrement nodes.",
                    NodeUtil.getFixedAttributes(node));
        }
        
    }
    
}
