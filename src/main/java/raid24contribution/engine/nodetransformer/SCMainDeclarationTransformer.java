package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCParameter;

/**
 * we have found the main-function so we serach for the parameters, handle them, get the return-type
 * and the name of this function and build a new function, which we add to the
 * global-systemfucntions we also get the block-node, where the function-body is described we saved
 * it for later
 * 
 */
public class SCMainDeclarationTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.setLocation("MAIN");
        e.getLastQualifiedId().clear();
        handleChildNodes(node, e);
        String name = e.getLastQualifiedId().pop();
        String type = e.getLastType().pop();
        
        List<SCParameter> clone = new ArrayList<>();
        for (SCParameter param : e.getLastParameterList()) {
            clone.add(param);
        }
        e.getLastParameterList().clear();
        
        SCFunction main = new SCFunction(name, type, clone);
        
        e.getSystem().addGlobalFunction(main);
        
        HashMap<String, List<Node>> existingFunctions = e.getFunctionBodys().get("system");
        if (existingFunctions == null) {
            existingFunctions = new HashMap<>();
        }
        existingFunctions.put(main.getName(), e.getLastFunctionBody());
        
        e.getFunctionBodys().put("system", existingFunctions);
        e.setLocation("");
    }
    
}
