package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;

/**
 * we get the type and add its name to the last_typeargumentslist
 * 
 */
public class TemplateArgumentTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        handleChildNodes(node, e);
        String type = e.getLastType().pop();
        e.getLastType_TemplateArguments().add(type);
    }
}
