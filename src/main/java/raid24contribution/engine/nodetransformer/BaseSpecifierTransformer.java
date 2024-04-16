package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;

/**
 * Handles the baseSpecifier tag, which represents the inheritance of modules and structs. Adds the
 * classes to the father objects of the current module or class and sets flags if we found
 * interfaces used by primitive channels or hierarchical channels.
 * 
 * 
 */
public class BaseSpecifierTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(BaseSpecifierTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        SCClass currentClass = e.getCurrentClass();
        if (currentClass != null) {
            handleInheritance(node, e, currentClass);
        } else {
            logger.error("{}: Encountered an inheritance modifier without any enclosing class.",
                    NodeUtil.getFixedAttributes(node));
        }
    }
    
    private void handleInheritance(Node node, Environment e, SCClass cl) {
        String name = NodeUtil.getAttributeValueByName(node, "name");
        if (name.equals("sc_module")) {
            // standard inheritation
            cl.addInheritFrom(new SCClass(name));
        } else if (name.equals("sc_channel") || name.equals("sc_interface")) {
            cl.addInheritFrom(new SCClass(name));
            cl.setHierarchicalChannel();
        } else if (name.equals("sc_prim_channel")) {
            cl.setPrimitiveChannel();
        } else {
            if (e.getClassList().containsKey(name)) {
                cl.addInheritFrom(e.getClassList().get(name));
            } else {
                // we found a new Module and hope that later in the XML-File
                // this module is specified
                // TODO: is this really necessary with our multi-phase approach?
                SCClass mod = new SCClass(name);
                e.getClassList().put(name, mod);
                cl.addInheritFrom(mod);
            }
        }
    }
}
