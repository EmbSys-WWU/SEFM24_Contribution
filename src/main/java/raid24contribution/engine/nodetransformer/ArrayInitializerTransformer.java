package raid24contribution.engine.nodetransformer;

import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.ArrayInitializerExpression;

/**
 * at the arrayinitializer-tag the Array is already declared now we need to handle the childnodes
 * every element is descripted as a expression every time we handle a childnode the
 * lastInitializer-Variable of the environment is set we take it and put it in the
 * ArrayInitializerExpression After we handled all childnodes, we push this expression in the stack
 * a multidimensional Array is initialized as array of array-initalizers
 *
 *
 */
public class ArrayInitializerTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        List<Node> nodeList = findChildNodes(node, "initializer");
        ArrayInitializerExpression arr = new ArrayInitializerExpression(node, nodeList.size());
        int i = 0;
        for (Node n : nodeList) {
            handleNode(n, e);
            
            arr.initAtPosition(i, e.getLastInitializer());
            i++;
            
        }
        e.getExpressionStack().push(arr);
    }
}
