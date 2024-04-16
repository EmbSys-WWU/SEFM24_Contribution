package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.RefDerefExpression;
import raid24contribution.sc_model.expressions.UnaryExpression;

/**
 * Transforms all nodes of the unary_operator type. For convenience we create two different
 * expressions here. The general unary expression and the more specialized refderefexpression for
 * all pointer dereferencing and variable referencing operations. This differentiation eases the
 * handling of pointers and referenced variables later on.
 * 
 */
public class UnaryExpressionTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        // usually a unary expression contains an operator (e.g., not)
        Node operator = findChildNode(node, "unary_operator");
        String op = NodeUtil.getAttributeValueByName(operator, "operator");
        
        // and a primary expression (e.g. (x))
        Node expression = findChildNode(node, "primary_expression");
        if (expression == null) {
            // however sometimes the expression is a postfix expression (e.g.,
            // port.read()).
            expression = findChildNode(node, "postfix_expression");
        }
        handleNode(expression, e);
        
        Expression exp = e.getExpressionStack().pop();
        if (op.equals("pointer")) {
            RefDerefExpression rde = new RefDerefExpression(node, exp, RefDerefExpression.DEREFERENCING);
            e.getExpressionStack().push(rde);
            
        } else if (op.equals("ref")) {
            RefDerefExpression rde = new RefDerefExpression(node, exp, RefDerefExpression.REFERENCING);
            e.getExpressionStack().push(rde);
            
        } else {
            if (op.equals("add")) {
                exp = new UnaryExpression(node, true, "+", exp);
            } else if (op.equals("minnus")) { // !
                exp = new UnaryExpression(node, true, "-", exp);
            } else if (op.equals("tilde")) {
                exp = new UnaryExpression(node, true, "~", exp);
            } else if (op.equals("not")) {
                exp = new UnaryExpression(node, true, "!", exp);
            }
            e.getExpressionStack().push(exp);
        }
        
    }
}
