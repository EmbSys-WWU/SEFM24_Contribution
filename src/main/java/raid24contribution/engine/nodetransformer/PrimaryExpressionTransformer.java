package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.AccessExpression;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.BracketExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.QuestionmarkExpression;

/**
 * Handles the primary_expression node. Usually this node can just be ignored and we only have to
 * handle the child nodes. However as the statement (x * y) % z can only be differentiated from x *
 * y % z by the order of the operation nodes AND by an extra primary expression where the brackets
 * are in the first example, we have to insert a bracketExpression if the child node returns a
 * binary expression.
 * 
 */
public class PrimaryExpressionTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        // saving the stacksize
        int previousSize = e.getExpressionStack().size();
        // handling the child nodes
        handleChildNodes(node, e);
        
        // check if there was only one child node
        if (e.getExpressionStack().size() == previousSize + 1) {
            // check if the last expression on the stack is a binary expression
            Expression exp = e.getExpressionStack().pop();
            // TODO is this really necessary or can we just add brackets to all expressions?
            if (exp instanceof AccessExpression || exp instanceof BinaryExpression
                    || exp instanceof QuestionmarkExpression) {
                // encapsulate the binary expression in a bracketExpression
                exp = new BracketExpression(exp.getNode(), exp);
            }
            
            // put the expression back on the stack
            e.getExpressionStack().add(exp);
        }
    }
}
