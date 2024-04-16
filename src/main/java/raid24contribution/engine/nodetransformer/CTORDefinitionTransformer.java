package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCParameter;

/**
 * with this tag we found the constructor of the current object we create a dummyfunction and parse
 * the parameters of the function we also save the body we create a new SCFunction add the parameter
 * and put it to the functionlist of the current object we also that the constructor-variable of the
 * current object
 * 
 * 
 */
public class CTORDefinitionTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(CTORDefinitionTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        
        if (e.getCurrentClass() != null) {
            generateConstructor(node, e, e.getCurrentClass());
        } else {
            logger.error("{}: Encountered a constructor without any enclosing struct or module.",
                    NodeUtil.getFixedAttributes(node));
        }
        
    }
    
    /**
     * Generates the constructor for the given class.
     * 
     * @param node
     * @param e
     * @param cl
     */
    private void generateConstructor(Node node, Environment e, SCClass cl) {
        e.getLastQualifiedId().clear();
        Node id = findChildNode(node, "qualified_id");
        handleNode(id, e);
        
        String name = (e.getLastQualifiedId().empty()) ? cl.getName() : e.getLastQualifiedId().pop();
        String type = cl.getName();
        
        SCFunction placeholder = new SCFunction(name, type);
        e.setCurrentFunction(placeholder);
        
        Node parameterList = findChildNode(node, "parameter_list");
        handleNode(parameterList, e);
        
        List<SCParameter> clone = new ArrayList<>();
        for (SCParameter param : e.getLastParameterList()) {
            clone.add(param);
        }
        e.getLastParameterList().clear();
        
        SCFunction constr = new SCFunction(name, type, clone);
        e.setCurrentFunction(constr);
        
        Node initializer = findChildNode(node, "ctor_initializer");
        handleNode(initializer, e);
        
        Node block = findChildNode(node, "block");
        handleNode(block, e);
        
        if (cl.getConstructor() != null && !cl.getConstructor().equals(constr)) {
            logger.error("{}: Encountered multiple constructors which are not identically.",
                    NodeUtil.getFixedAttributes(node));
        } else {
            cl.addMemberFunction(constr);
            cl.setConstructor(constr);
            
            e.setLocation("constructor");
            HashMap<String, List<Node>> existingFunctions = e.getFunctionBodys().get(e.getCurrentClass().getName());
            if (existingFunctions == null) {
                existingFunctions = new HashMap<>();
            }
            
            existingFunctions.put(constr.getName(), e.getLastFunctionBody());
            e.getFunctionBodys().put(e.getCurrentClass().getName(), existingFunctions);
        }
        
        e.setLocation("");
        e.setCurrentFunction(null);
    }
    
}
