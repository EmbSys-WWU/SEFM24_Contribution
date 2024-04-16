package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.TimeUnitExpression;
import raid24contribution.sc_model.variables.SCTIMEUNIT;

/**
 * if the tag has a value-attribute, we extract it, look weather there are subinformations and
 * create a new constant-expression if we found out, thats a Timeunit, we create a new Variable and
 * a new variable-expression no matter what it is, we push the expression on the stack
 * 
 * if it hasn't a value-attribute, we only handle the childnodes
 */
public class ConstantTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        String value = NodeUtil.getAttributeValueByName(node, "value");
        if (value != null) {
            long intVal = 0;
            try {
                if (value.indexOf("0x") == 0) {
                    intVal = Long.parseLong(value.substring(2), 16);
                    value = new Long(intVal).toString();
                } else if (value.indexOf("-0x") == 0) {
                    intVal = -Long.parseLong(value.substring(3), 16);
                    value = new Long(intVal).toString();
                }
            } catch (Exception exc) {
            }
            
            Expression exp;
            
            if (SCTIMEUNIT.containsValue(value)) {
                exp = new TimeUnitExpression(node, "", SCTIMEUNIT.valueOf(value));
            } else {
                exp = new ConstantExpression(node, value);
            }
            
            // e.operandStack.push(value);
            e.getExpressionStack().push(exp);
        } else {
            handleChildNodes(node, e);
        }
    }
    
}
