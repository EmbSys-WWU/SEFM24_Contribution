package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;

/**
 * we clear the type and the modifiers at this point then we handle the childnodes where this
 * variables should be set
 * 
 */
public class MemberDeclarationTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.setFoundMemberType("");
        e.getFoundTypeModifiers().clear();
        handleChildNodes(node, e);
    }
}
