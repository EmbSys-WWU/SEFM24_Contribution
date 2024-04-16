package raid24contribution.engine.nodetransformer;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.QuestionmarkExpression;

/**
 * a questionmark-expression is similar to an ifElseExpression first we get all real childnodes, if
 * we found less then 3 we have to few then we get the first three nodes, the first is the condition
 * the second the if-case the third the else-case we handle all and remember the found expression
 * with this three expressions we build the new questionmarkexpression
 * 
 */
public class QuestionmarkExpressionTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(QuestionmarkExpressionTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        List<Node> nodes = findRealChildNodes(node);
        if (nodes.size() < 3) {
            logger.error("{}: Questionmark expression is missing one or more child-expressions.",
                    NodeUtil.getFixedAttributes(node));
            return;
        }
        Node cond = nodes.remove(0);
        Node ifTrue = nodes.remove(0);
        Node ifFalse = nodes.remove(0);
        
        handleNode(cond, e);
        Expression exp_cond = e.getExpressionStack().pop();
        
        handleNode(ifTrue, e);
        Expression exp_if = e.getExpressionStack().pop();
        
        handleNode(ifFalse, e);
        Expression exp_else = e.getExpressionStack().pop();
        
        QuestionmarkExpression qe = new QuestionmarkExpression(node, exp_cond, exp_if, exp_else);
        e.getExpressionStack().push(qe);
    }
    
}
