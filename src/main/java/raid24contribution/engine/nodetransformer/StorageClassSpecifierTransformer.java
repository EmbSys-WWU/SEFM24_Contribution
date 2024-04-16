package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;

/**
 * we get the name-attribute from this tag, its a type-modifier
 * 
 */
public class StorageClassSpecifierTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.getFoundTypeModifiers().add(NodeUtil.getAttributeValueByName(node, "name"));
        handleChildNodes(node, e);
    }
}
