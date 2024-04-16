package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.typetransformer.KnownTypeTransformer;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCREFERENCETYPE;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.variables.SCArray;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.sc_model.variables.SCEvent;
import raid24contribution.sc_model.variables.SCPointer;
import raid24contribution.sc_model.variables.SCSimpleType;
import raid24contribution.sc_model.variables.SCTime;

/**
 * first we get the childe-nodes named declaration_specifiers and declarator if the declarator
 * doesn't exist, we have no parameter otherwise we get the qualified_id-childenode, it it exist, we
 * have a pointer then we get the pointertype and save it for later-construction
 * 
 * next step is to get the subtypes, the name and the type then we create a Variable, correspondig
 * to the type, which represent the parameter and add it to the list
 * 
 */
public class ParameterDeclarationTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(ParameterDeclarationTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        // handleChildNodes(n, e);
        // SCVariable scvar = new SCVariable(e.lastQualifiedId.pop(),
        // e.lastType.pop());
        // e.lastParameterList.add(scvar);
        
        handleNode(findChildNode(node, "declaration_specifiers"), e);
        Node declarator = findChildNode(node, "declarator");
        Node suffixes = null;
        
        if (declarator != null) {
            Node id = findChildNode(declarator, "qualified_id");
            boolean reference = false;
            boolean dereference = false;
            if (id == null) {
                Node ptr = findChildNode(declarator, "ptr_operator");
                Node decl2 = findChildNode(declarator, "declarator");
                suffixes = findChildNode(decl2, "declarator_suffixes");
                id = findChildNode(decl2, "qualified_id");
                if (ptr != null && decl2 != null && id != null) {
                    String ptrName = NodeUtil.getAttributeValueByName(ptr, "name");
                    if (ptrName.equals("&")) {
                        reference = true;
                    } else if (ptrName.equals("*")) {
                        // we found a derefence operator, perhaps from the
                        // "void set_auto_extension(void* x)"
                        // void-Pointer-Parameter which every data send via
                        // sockets has to implement.
                        // i've got no idea how to do this better than just
                        // ignoring it.
                        dereference = true;
                    } else {
                        logger.error("{}: Unsupported pointer type in parameter definition.",
                                NodeUtil.getFixedAttributes(node));
                        return;
                    }
                } else {
                    logger.error("{}: Parameter definition is too complex. Not supported.",
                            NodeUtil.getFixedAttributes(node));
                    return;
                }
                
            }
            
            List<String> temp_Args = new ArrayList<>();
            for (String subtype : e.getLastType_TemplateArguments()) {
                temp_Args.add(subtype);
            }
            e.getLastType_TemplateArguments().clear();
            
            String paramName = NodeUtil.getAttributeValueByName(id, "name");
            
            String type = e.getLastType().pop();
            
            SCVariable var = null;
            
            if (type != null) {
                if (suffixes != null) {
                    // the parameter is an array, therefore it cannot be
                }
                if (suffixes == null) {
                    // the parameter is not an array
                    if (dereference == true) {
                        // the parameter is a pointer
                        var = new SCPointer(paramName, type, false, false, new ArrayList<>(), null);
                    } else if (e.getTransformerFactory().isSimpleType(type)) {
                        var = new SCSimpleType(paramName, type, false, false, new ArrayList<>());
                    } else if (type.equals("sc_event")) {
                        var = new SCEvent(paramName, false, false, new ArrayList<>());
                    } else if (type.equals("sc_time")) {
                        var = new SCTime(paramName, false, false, null, false);
                    } else if (!e.getClassList().isEmpty() && e.getClassList().containsKey(type)) {
                        var = new SCClassInstance(paramName, e.getClassList().get(type), e.getCurrentClass());
                    } else if (!e.getKnownTypes().isEmpty() && e.getKnownTypes().containsKey(type)) {
                        KnownTypeTransformer typeTrans = e.getTransformerFactory().getTypeTransformer(type, e);
                        if (typeTrans != null) {
                            var = typeTrans.createInstance(paramName, e, false, false, new ArrayList<>());
                        }
                    } else if (e.getTransformerFactory().isKnownType(type)) {
                        KnownTypeTransformer typeTrans = e.getTransformerFactory().getTypeTransformer(type, e);
                        if (typeTrans != null) {
                            typeTrans.createType(e);
                            var = typeTrans.createInstance(paramName, e, false, false, new ArrayList<>());
                        }
                    } else {
                        // it's a struct which we havn't found yet
                        SCClass s = new SCClass(type);
                        e.getClassList().put(type, s);
                        var = new SCClassInstance(paramName, s, e.getCurrentClass());
                    }
                } else {
                    if (dereference == true) {
                        // TODO mp: i don't think there should be a *.
                        var = new SCPointer(paramName, type + "*", false, false, new ArrayList<>(), null);
                    } else {
                        var = new SCArray(paramName, type);
                    }
                }
            }
            
            if (var == null) {
                logger.warn("Creating parameter with no variable: {}", NodeUtil.getFixedAttributes(node));
            }
            
            SCParameter param =
                    new SCParameter(var, (reference ? SCREFERENCETYPE.BYREFERENCE : SCREFERENCETYPE.BYVALUE));
            e.getLastParameterList().add(param);
            
        } else {
            if (e.getLastType().peek().equals("void")) {
                e.getLastType().pop();
            }
        }
    }
    
}
