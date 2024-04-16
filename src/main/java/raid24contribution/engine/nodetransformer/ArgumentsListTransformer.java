package raid24contribution.engine.nodetransformer;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.Expression;

/**
 * Handles the arguments of a function call and stores them in environment.lastArgumentList in the
 * same order they appear
 */
public class ArgumentsListTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        int s = e.getExpressionStack().size();
        handleChildNodes(node, e);
        List<Expression> arguments = new LinkedList<>();
        while (s != e.getExpressionStack().size()) {
            arguments.add(e.getExpressionStack().get(s));
            e.getExpressionStack().remove(s);
        }
        e.setLastArgumentList(arguments);
    }
}
