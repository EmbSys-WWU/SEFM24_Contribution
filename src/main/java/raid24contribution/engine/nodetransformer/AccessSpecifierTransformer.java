package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;

/**
 * this class saves the current access-level in the appropriate environment-variable
 *
 *
 */
public class AccessSpecifierTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        String access = NodeUtil.getAttributeValueByName(node, "name");
        e.setCurrentAccessKey(access);
    }
    
    
}
