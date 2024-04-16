package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.Expression;

/**
 * an assignment-tag has 2 childnodes, the first childnode is the left-element where the
 * right-element, the second childnode, is assigned to we save an assignment as binary-expression
 * after the childnodehandling the topelement of the stack is the right-element and the next element
 * is the left-element if we have a childnode named assignment_expression_operator we take the
 * assignment-operator from him, otherwise its the "="
 *
 */
public class AssignmentTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(AssignmentTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        handleChildNodes(node, e);
        Node opNode = findChildNode(node, "assignment_expression_operator");
        String op = "=";
        if (opNode != null) {
            op = NodeUtil.getAttributeValueByName(opNode, "name");
        }
        
        if (e.getExpressionStack().size() < 2) {
            logger.error("{}: Invalid assignment", NodeUtil.getFixedAttributes(node));
            return;
        }
        Expression exp_rgt = e.getExpressionStack().pop();
        Expression exp_lft = e.getExpressionStack().pop();
        
        BinaryExpression bin_exp = new BinaryExpression(node, exp_lft, op, exp_rgt);
        
        e.getExpressionStack().push(bin_exp);
        
    }
}
