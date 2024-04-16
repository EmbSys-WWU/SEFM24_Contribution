package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;

/**
 * we get the name of the function which is declared as method and put it to the corresponding
 * environment-variable
 * 
 */
public class SCMethodTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        String func = NodeUtil.getAttributeValueByName(node, "name");
        e.setLastProcessName(func);
        e.setLastProcessFunction(e.getCurrentClass().getMemberFunctionByName(func));
    }
}
