package raid24contribution.engine.nodetransformer;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.typetransformer.KnownTypeTransformer;
import raid24contribution.engine.util.NodeUtil;

/**
 * In this class we handle the BuiltinTypeSpecifier-Tag we extract the name of the type and if some
 * exist, the suptype and the length
 * 
 */
public class BuiltinTypeSpecifierTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(BuiltinTypeSpecifierTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        List<Node> templates = findChildNodes(node, "declaration_specifiers");
        
        String type = NodeUtil.getAttributeValueByName(node, "name");
        String length = NodeUtil.getAttributeValueByName(node, "length");
        
        e.getLastType_TemplateArguments().clear();
        for (Node n : templates) {
            handleNode(n, e);
            String subType = e.getLastType().pop();
            e.getLastType_TemplateArguments().add(subType);
        }
        
        if (e.getTransformerFactory().isSimpleType(type)) {
            // nothing
        } else if (type.equals("void")) {
            // nothing
        } else if (type.equals("sc_event")) {
            // nothing
        } else if (type.equals("sc_module_name")) {
            // ignore
        } else if (type.equals("peq_with_cb_and_phase")) {
            /* // nothing to do here */
        } else if (type.equals("sc_time")) {
            // nothing
        } else if (type.equals("tlm_dmi")) {
            logger.debug("Creating dummy tlm_dmi type.");
        } else {
            
            if (e.getKnownTypes().containsKey(type)) {
                
            } else {
                KnownTypeTransformer tpTrans = e.getTransformerFactory().getTypeTransformer(type, e);
                if (tpTrans != null) {
                    tpTrans.createType(e);
                } else {
                    logger.error("{}: Builtin type '{}' is not supported.", NodeUtil.getFixedAttributes(node), type);
                    type = null;
                }
            }
        }
        if (length != null && !length.equals("0")) {
            type = type + "<" + length + ">";
        }
        
        e.getLastType().push(type);
        
    }
    
}
