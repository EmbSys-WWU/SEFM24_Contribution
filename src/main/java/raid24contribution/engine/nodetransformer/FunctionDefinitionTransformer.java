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
 * first we handle the childnodes of the function their we found the returntype, the name, the
 * parameter and the scope-overwrite if their isn't a scope-overwrite, we have a normal function, so
 * we create a new one we have to check if we are in a module, a struct, or if its a global
 * function, we add it at the right place and create a new entry in the functionbody-map if
 * necessary and add the functionbody if their is a scope we do nearly the same, but we look for a
 * module or a struct with the name of the scope and add the function their if we found one if we
 * didn't found a module with this name neither a struct, we assume its a global function
 * 
 */
public class FunctionDefinitionTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(FunctionDefinitionTransformer.class.getName());
    
    @Override
    public void transformNode(Node n, Environment e) {
        e.setLastScopeOverwrite("");
        handleChildNodes(n, e);
        
        List<SCParameter> clone = new ArrayList<>();
        for (SCParameter param : e.getLastParameterList()) {
            clone.add(param);
        }
        e.getLastParameterList().clear();
        
        SCFunction scfct = null;
        String scope = e.getLastScopeOverwrite();
        String funName = e.getLastQualifiedId().pop();
        
        
        if (scope.equals("")) {
            // we have the parameters, the name and the return type but the
            // return type might be a pointer as KaSCPar adds the * to the
            // function name instead of to the return type
            // this * can be found inside the function_declarator node
            // the function declarator node is always there as it contains the
            // name of the function
            Node funDecl = findChildNode(n, "function_declarator");
            // inside the node is an ptr_operator node if the return type of the
            // function is a pointer instead of a normal variable
            Node ptrOp = findChildNode(funDecl, "ptr_operator");
            String returnType = e.getLastType().pop();
            if (ptrOp != null) {
                String op = NodeUtil.getAttributeValueByName(ptrOp, "name");
                if (op.equals("*")) {
                    returnType += "*";
                } else {
                    logger.error("{}: Encountered unsupported modifier to the return value of function {}: ",
                            NodeUtil.getFixedAttributes(n), funName, op);
                }
            }
            scfct = new SCFunction(funName, returnType, clone);
            if (e.getCurrentClass() != null) {
                e.getCurrentClass().addMemberFunction(scfct);
                HashMap<String, List<Node>> existingFunctions = e.getFunctionBodys().get(e.getCurrentClass().getName());
                if (existingFunctions == null) {
                    existingFunctions = new HashMap<>();
                }
                existingFunctions.put(scfct.getName(), e.getLastFunctionBody());
                e.getFunctionBodys().put(e.getCurrentClass().getName(), existingFunctions);
            } else {
                e.getSystem().addGlobalFunction(scfct);
                HashMap<String, List<Node>> existingFunctions = e.getFunctionBodys().get("system");
                if (existingFunctions == null) {
                    existingFunctions = new HashMap<>();
                }
                existingFunctions.put(scfct.getName(), e.getLastFunctionBody());
                
                e.getFunctionBodys().put("system", existingFunctions);
            }
            
        } else {
            SCClass str = e.getClassList().get(scope);
            
            if (str != null) {
                scfct = str.getMemberFunctionByName(funName);
                HashMap<String, List<Node>> existingFunctions = e.getFunctionBodys().get(str.getName());
                if (existingFunctions == null) {
                    existingFunctions = new HashMap<>();
                }
                existingFunctions.put(scfct.getName(), e.getLastFunctionBody());
                e.getFunctionBodys().put(str.getName(), existingFunctions);
            } else {
                // create new function
                scfct = new SCFunction(funName);
                e.getSystem().addGlobalFunction(scfct);
                HashMap<String, List<Node>> existingFunctions = e.getFunctionBodys().get("system");
                if (existingFunctions == null) {
                    existingFunctions = new HashMap<>();
                }
                existingFunctions.put(scfct.getName(), e.getLastFunctionBody());
                
                e.getFunctionBodys().put("system", existingFunctions);
                
            }
        }
        
    }
}
