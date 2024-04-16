package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;

/**
 * the Block-tag contains the body of a function we save all Childnodes and put them into a list so
 * we can handle them later but if we ar already inside of a functionblock , we handle this
 * chidlnodes
 * 
 * 
 */
public class BlockTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.setLastFunctionBody(getAllChildNodes(node));
        
        if (e.isFunctionBlock()) {
            e.getExpressionStack().clear();
            e.setLastFunctionBody(getAllChildNodes(node));
        } else {
            handleChildNodes(node, e);
        }
        
    }
    
}
