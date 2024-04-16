package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;

/**
 * Handles the class specifier node. This node always occures when a class (module or struct) is
 * declared. As it is possible to declare a class inside of another one, we have to make sure that
 * we store the surrounding class and restore it afterwards.
 * 
 * 
 */
public class ClassSpecifierTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        String className = NodeUtil.getAttributeValueByName(node, "name");
        String keyword = NodeUtil.getAttributeValueByName(node, "keyword");
        // boolean extSCModule = Boolean.parseBoolean(NodeUtil
        // .getAttributeValueByName(node, "extSCModule"));
        
        // seems as if KaSCPar can't differantiate between structs and classes
        // (both get the class-keyword), so we might not need this check.
        if (keyword.equals("class")) {
            e.setCurrentAccessKey("private");
        } else if (keyword.equals("struct")) {
            e.setCurrentAccessKey("public");
        }
        
        // save the current class (the surrounding class)
        SCClass temp = e.getCurrentClass();
        SCClass cl = null;
        if (e.getClassList().containsKey(className)) {
            // this module was found as Father of an other module,
            // so it exists already in the list of modules in the
            // environment
            cl = e.getClassList().get(className);
            
        } else {
            // we found a new class
            cl = new SCClass(className);
            e.getClassList().put(className, cl);
            e.getSystem().addClass(cl);
        }
        
        // parse the class
        e.setCurrentClass(cl);
        e.getFunctionBodys().put(cl.getName(), null);
        handleChildNodes(node, e);
        
        // restore surrounding class
        e.setCurrentClass(temp);
    }
    
}
