package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCPort;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.ArrayAccessExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.MultiSocketAccessExpression;
import raid24contribution.sc_model.expressions.SCPortSCSocketExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.variables.SCArray;
import raid24contribution.sc_model.variables.SCPointer;

/**
 * every time an Array is accessed, we need to know the indices first we need all
 * primary-expressions in the first, the Array is specified the other ones specifies the indices we
 * handle the childenodes of every primary-tag after we handle the first primary-tag we have an
 * variable-expression on the stack, we check if we have an array if its true, we keep it, and set a
 * boolean true so all other primary-expressions contain the index-descriptions we put them into a
 * list then we build a new ArrayAccessExpression and push it on the stack
 * 
 * @author Florian
 * 
 */
public class ArrayAccessExpressionTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(ArrayAccessExpressionTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        // the first primary expression inside the access node is the array itself
        Node arrayNode = findChildNode(node, "primary_expression");
        handleChildNodes(arrayNode, e);
        Expression exp = e.getExpressionStack().pop();
        SCVariable arrOrPtr = null;
        SCPort ps = null;
        if (exp instanceof SCVariableExpression ve) {
            if (ve.getVar() instanceof SCArray || ve.getVar() instanceof SCPointer) {
                arrOrPtr = ve.getVar();
            } else {
                logger.error("{}: Only Arrays or Pointers can be accessed in a ArrayAccessExpressionTransformer",
                        NodeUtil.getFixedAttributes(node));
            }
        } else if (exp instanceof SCPortSCSocketExpression pse) {
            ps = pse.getSCPortSCSocket();
        }
        
        // now, handle the access inside
        NodeList accessNodes = node.getChildNodes();
        List<Expression> exp_list = new ArrayList<>();
        int start = 1; // first child is array itself
        if (isIgnorableWhitespaceNode(accessNodes.item(0))) {
            start++; // skip twice
        }
        for (int i = start; i < accessNodes.getLength(); i++) {
            Node n = accessNodes.item(i);
            if (!isIgnorableWhitespaceNode(n)) {
                handleNode(n, e);
                exp_list.add(e.getExpressionStack().pop());
            }
        }
        
        // put everything together
        Expression ret = null;
        if (arrOrPtr != null) {
            ret = new ArrayAccessExpression(node, arrOrPtr, exp_list);
        } else {
            ret = new MultiSocketAccessExpression(node, ps, exp_list);
        }
        e.getExpressionStack().push(ret);
    }
}
