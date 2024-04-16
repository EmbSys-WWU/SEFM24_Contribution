package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.expressions.AccessExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.SCPortSCSocketExpression;
import raid24contribution.sc_model.variables.SCEvent;
import raid24contribution.sc_model.variables.SCPortEvent;
import raid24contribution.sc_model.variables.SCVariableEvent;

public class SCSensitiveTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(SCSensitiveTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            // default events
            String nodeName = child.getNodeName();
            if (nodeName.equals("qualified_id")) {
                String id = NodeUtil.getAttributeValueByName(child, "name");
                SCClass cl = e.getCurrentClass();
                if (cl.getEventByName(id) != null) {
                    e.getSensitivityList().add(cl.getEventByName(id));
                } else if (cl.getPortSocketByName(id) != null) {
                    e.getSensitivityList().add(new SCPortEvent(id, cl.getPortSocketByName(id)));
                } else if (cl.getMemberByName(id) != null) {
                    e.getSensitivityList()
                            .add(new SCVariableEvent(id, new SCVariableEvent(id, cl.getMemberByName(id))));
                } else {
                    logger.debug(NodeUtil.getFixedAttributes(child));
                    logger.error(
                            "{}: Encountered sensitivity to {}, which is neither a port or socket, nor an event or a variable.",
                            NodeUtil.getFixedAttributes(node), id);
                    return;
                }
                
                // other event types (e.g., clock.pos())
            } else if (nodeName.equals("postfix_expression")) {
                handleNode(child, e);
                Expression exp = e.getExpressionStack().pop();
                if (exp instanceof AccessExpression ae) {
                    if (ae.getLeft() instanceof SCPortSCSocketExpression
                            && ae.getRight() instanceof FunctionCallExpression) {
                        SCPortSCSocketExpression scps = (SCPortSCSocketExpression) ae.getLeft();
                        FunctionCallExpression fce = (FunctionCallExpression) ae.getRight();
                        SCEvent ev = new SCPortEvent(scps.getSCPortSCSocket().getName(), scps.getSCPortSCSocket(),
                                fce.getFunction().getName());
                        e.getSensitivityList().add(ev);
                    } else {
                        logger.error("{}: Encountered an unknown event type.", NodeUtil.getFixedAttributes(node));
                    }
                }
            }
        }
    }
}
