package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.EventNotificationExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.variables.SCEvent;

/**
 * Handles the SCNotify node. These nodes represent the notification of an event.
 * 
 */
public class SCNotifyTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(SCNotifyTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        String ev = NodeUtil.getAttributeValueByName(node, "name");
        if (e.getCurrentClass() != null) {
            SCClass mod = e.getCurrentClass();
            SCEvent evt = mod.getEventByName(ev);
            SCVariableExpression evt_exp;
            if (evt != null) {
                evt_exp = new SCVariableExpression(node, evt);
            } else {
                SCVariable var = mod.getMemberByName(ev);
                evt_exp = new SCVariableExpression(node, var);
            }
            
            Node arguments = findChildNode(node, "arguments_list");
            ArrayList<Expression> args = new ArrayList<>();
            if (arguments != null) {
                int s = e.getExpressionStack().size();
                handleChildNodes(arguments, e);
                
                while (s != e.getExpressionStack().size()) {
                    args.add(e.getExpressionStack().get(s));
                    e.getExpressionStack().remove(s);
                }
            }
            
            EventNotificationExpression ene = new EventNotificationExpression(node, evt_exp, args);
            e.getExpressionStack().add(ene);
            
            e.getCurrentFunction().addEventNotification(ene);
        } else {
            logger.error("{}: Encountered notification of event outside of a class. Eventname: {}",
                    NodeUtil.getFixedAttributes(node), ev);
        }
        
    }
}
