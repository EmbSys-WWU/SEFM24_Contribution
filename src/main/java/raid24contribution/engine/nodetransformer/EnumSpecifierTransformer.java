package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCEnumType;
import raid24contribution.sc_model.expressions.Expression;

/**
 * This class handles EnumSpecifier nodes. It is used when an enum types is defined in the model and
 * creates a new enumType in the system. Since the child nodes of an EnumSpecifier node are known,
 * the "enumerator_list" and the "enumerator child" nodes are also handled in this transformer.
 * 
 */
public class EnumSpecifierTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(EnumSpecifierTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        String enumName = NodeUtil.getAttributeValueByName(node, "name");
        
        if (e.getKnownTypes().isEmpty() || !e.getKnownTypes().containsKey(enumName)) {
            SCEnumType enumType = new SCEnumType(enumName);
            e.getTransformerFactory().addEnumType(enumName);
            
            Node enumListNode = null; // "enumerator_list"
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                if (node.getChildNodes().item(i).getNodeName().equals("enumerator_list")) {
                    enumListNode = node.getChildNodes().item(i);
                    break;
                }
            }
            
            NodeList enumerators = enumListNode.getChildNodes();
            
            // handle all child nodes and create a new enum element for each
            // "enumerator" node
            for (int i = 0; i < enumerators.getLength(); i++) {
                
                Node enumNode = enumerators.item(i);
                if (!enumNode.getNodeName().equals("enumerator")) {
                    continue;
                }
                
                String expressionName = NodeUtil.getAttributeValueByName(enumNode, "name");
                
                // A defined value is given as child node of the enumerator
                // node. It can be handled like an expression.
                int currentStackSize = e.getExpressionStack().size();
                handleChildNodes(enumNode, e);
                // if the child node handling does not put an expression on the
                // stack, there is no defined value given for this enum element
                if (currentStackSize < e.getExpressionStack().size()) {
                    // defined value is given
                    Expression ie = e.getExpressionStack().pop();
                    
                    // we only support Integer values as defined values for enum
                    // elements
                    try {
                        int iv = Integer.parseInt(ie.toStringNoSem());
                        
                        enumType.addElement(expressionName, iv);
                    } catch (NumberFormatException nfe) {
                        logger.error(
                                "Cannot parse the defined value for enum element {} in enum type {}, only Integer values are supported",
                                expressionName, enumName);
                    }
                } else {
                    // no defined value is given
                    enumType.addElement(expressionName);
                }
            }
            e.getSystem().addEnumType(enumType);
        }
    }
    
}
