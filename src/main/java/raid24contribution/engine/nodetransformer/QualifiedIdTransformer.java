package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;

/**
 * we get the name-attribute from the current node and push that on the qualified_id-stack if this
 * node has a child-node, named scope, we get the scope and set the corresponding
 * environment-variable
 * 
 */

public class QualifiedIdTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.getLastQualifiedId().push(NodeUtil.getAttributeValueByName(node, "name"));
        Node n = findChildNode(node, "scope_override");
        if (n != null) {
            String scope = NodeUtil.getAttributeValueByName(n, "name");
            e.setLastScopeOverwrite(scope);
        }
        
    }
    
}
