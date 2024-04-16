package raid24contribution.engine.nodetransformer;

import java.util.LinkedList;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;

/**
 * first we get se childnode named qualified-id and get the name-attribute, this is the type then we
 * search for the template_argument_list, which ware the subtypes, and handle them
 * 
 */
public class QualifiedTypeTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        Node id = findChildNode(node, "qualified_id");
        String type = NodeUtil.getAttributeValueByName(id, "name");
        
        e.getLastType().push(type);
        
        Node tempArgs = findChildNode(id, "template_argument_list");
        if (tempArgs != null) {
            e.setLastType_TemplateArguments(new LinkedList<>());
            handleNode(tempArgs, e);
        } else {
            e.setLastType_TemplateArguments(new LinkedList<>());
        }
    }
    
}
