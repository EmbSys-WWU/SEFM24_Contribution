package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.typetransformer.SCFifoTypeTransformer;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCPort;
import raid24contribution.sc_model.SCREFERENCETYPE;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.RefDerefExpression;
import raid24contribution.sc_model.expressions.SCPortSCSocketExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.sc_model.variables.SCKnownType;
import raid24contribution.sc_model.variables.SCPeq;
import raid24contribution.sc_model.variables.SCPointer;
import raid24contribution.sc_model.variables.SCSimpleType;

/**
 * Handles the superclassinit node. This node is used in many different cases as it occurs whenever
 * a "." is used in the code (mostly field accesses).
 * 
 * 
 */
public class SuperClassInitTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(SuperClassInitTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        Node type = findChildNode(findChildNode(node, "declaration_specifiers"), "builtin_type_specifier");
        Node id = findChildNode(node, "qualified_id");
        List<Node> ls = findChildNodes(findChildNode(node, "arguments_list"), "primary_expression");
        handleNode(findChildNode(node, "arguments_list"), e);
        List<Expression> args = e.getLastArgumentList();
        e.setLastArgumentList(new LinkedList<>());
        
        if (type != null) {
            
            // its a superclass_constructor_call
            String typeName = NodeUtil.getAttributeValueByName(type, "name");
            if (typeName.equals("sc_module")) {
                // This is some special handling. In fact sc_module() should be
                // a function of sc_module but this would make things more
                // complicated.
                SCFunction fct = new SCFunction("sc_module", null);
                
                List<Expression> clone = new ArrayList<>();
                for (Expression expr : args) {
                    clone.add(expr);
                }
                e.setLastArgumentList(new LinkedList<>());
                
                FunctionCallExpression exp = new FunctionCallExpression(node, fct, clone);
                
                e.getCurrentFunction().addExpressionAtEnd(exp);
                return;
            } else if (typeName.equals("sc_channel")) {
                // This is some special handling. In fact sc_channel() should be
                // a function of sc_channel but this would make things more
                // complicated.
                SCFunction fct = new SCFunction("sc_channel", null);
                
                List<Expression> clone = new ArrayList<>();
                for (Expression expr : args) {
                    clone.add(expr);
                }
                e.getLastArgumentList().clear();
                
                FunctionCallExpression exp = new FunctionCallExpression(node, fct, clone);
                
                e.getCurrentFunction().addExpressionAtEnd(exp);
                return;
            } else {
                List<SCClass> inheritance = e.getCurrentClass().getInheritFrom();
                for (SCClass m : inheritance) {
                    if (m.getName().equals(typeName)) {
                        
                        List<Expression> clone = new ArrayList<>();
                        for (Expression expr : args) {
                            clone.add(expr);
                        }
                        e.getLastArgumentList().clear();
                        FunctionCallExpression exp = new FunctionCallExpression(node, m.getConstructor(), clone);
                        e.getCurrentFunction().addExpressionAtEnd(exp);
                        return;
                    }
                }
            }
        } else if (id != null) {
            // its a Memberinitiation
            String memberName = NodeUtil.getAttributeValueByName(id, "name");
            // this should always work
            SCClassInstance clI = e.getCurrentClass().getInstanceByName(memberName);
            if (clI != null) {
                
                List<Expression> clone = new ArrayList<>();
                for (Expression expr : args) {
                    clone.add(expr);
                }
                e.getLastArgumentList().clear();
                
                FunctionCallExpression exp = new FunctionCallExpression(node, clI.getSCClass().getConstructor(), clone);
                
                e.getCurrentFunction().addExpressionAtEnd(exp);
                return;
            } else {
                SCVariable var = e.getCurrentClass().getMemberByName(memberName);
                if (var != null) {
                    if (var instanceof SCClassInstance ci) {
                        List<Expression> clone = new ArrayList<>();
                        clone.addAll(args);
                        e.getLastArgumentList().clear();
                        
                        // special case for sc_fifo with non-standard size
                        if (ci instanceof SCKnownType && ((SCKnownType) ci).getSCClass().getName().startsWith("sc_fifo")
                                && !clone.isEmpty()) {
                            // create new sc_fifo with right size
                            SCClass scClass = ((SCKnownType) ci).getSCClass();
                            SCClass clonedClass = scClass.createClone();
                            String size = ((ConstantExpression) clone.get(0)).getValue();
                            SCFifoTypeTransformer.addSize(size, clonedClass);
                            
                            String className = SCFifoTypeTransformer.appendSize(clonedClass.getName(), size);
                            clonedClass.setName(className);
                            ci.setType(className);
                            ci.setSCClass(clonedClass);
                        }
                        
                        FunctionCallExpression exp =
                                new FunctionCallExpression(node, ci.getSCClass().getConstructor(), clone);
                        SCVariableExpression var_exp = new SCVariableExpression(node, var);
                        BinaryExpression be = new BinaryExpression(node, var_exp, "=", exp);
                        // ci.setInitialExpression(clone);
                        e.getCurrentFunction().addExpressionAtEnd(be);
                        // add instance to the system
                        e.getSystem().addInstance(ci);
                        return;
                        
                    } else {
                        handleChildNodes(ls.get(0), e);
                        
                        SCVariable v = var;
                        SCVariableExpression ve = new SCVariableExpression(node, v);
                        
                        List<Expression> clone = new ArrayList<>();
                        clone.addAll(args);
                        
                        // Peq binding
                        if (var instanceof SCPeq) {
                            if (args.size() == 3) {
                                Expression expr = args.get(2);
                                if (expr instanceof RefDerefExpression && ((RefDerefExpression) expr)
                                        .getExpression() instanceof FunctionCallExpression) {
                                    ((SCPeq) var).setCallback(
                                            ((FunctionCallExpression) ((RefDerefExpression) expr).getExpression())
                                                    .getFunction());
                                }
                            }
                        }
                        
                        // We have to handle classes (which can have a
                        // constructor) different to primitive data types.
                        if (e.getClassList().containsKey(v.getType())) {
                            Expression exp = null;
                            if (v instanceof SCPointer) {
                                // a ptr to a struct: init can only be first arg
                                // (hopefully^^)
                                exp = clone.get(0);
                            } else {
                                SCFunction dummyconstr = new SCFunction(v.getType(), v.getType());
                                exp = new FunctionCallExpression(node, dummyconstr, clone);
                            }
                            BinaryExpression bin_exp = new BinaryExpression(node, ve, "=", exp);
                            e.getCurrentFunction().addExpressionAtEnd(bin_exp);
                        } else {
                            if (!clone.isEmpty()) {
                                // v.setFirstInitialExpression(clone.get(0));
                                BinaryExpression be = new BinaryExpression(node, ve, "=", clone.get(0));
                                e.getCurrentFunction().addExpressionAtEnd(be);
                            } else {
                                logger.warn("trying to create binary expression from empty clone: {}",
                                        NodeUtil.getFixedAttributes(node));
                            }
                        }
                    }
                    
                } else {
                    
                    SCPort ps = e.getCurrentClass().getPortSocketByName(memberName);
                    if (ps != null) {
                        List<Expression> clone = new ArrayList<>();
                        for (Expression expr : args) {
                            clone.add(expr);
                        }
                        e.getLastArgumentList().clear();
                        
                        List<SCParameter> parameter = new ArrayList<>();
                        SCParameter param = new SCParameter(new SCSimpleType("socket type"), SCREFERENCETYPE.BYVALUE);
                        parameter.add(param);
                        
                        SCFunction socket_constr = new SCFunction(memberName, ps.getConType().toString(), parameter);
                        
                        FunctionCallExpression exp = new FunctionCallExpression(node, socket_constr, clone);
                        
                        SCPortSCSocketExpression pse = new SCPortSCSocketExpression(node, ps);
                        
                        BinaryExpression bin_exp = new BinaryExpression(node, pse, "=", exp);
                        e.getCurrentFunction().addExpressionAtEnd(bin_exp);
                        return;
                        
                    } else {
                        
                        logger.error("{}: Initiation of an object found, which isn't a member of the module",
                                NodeUtil.getFixedAttributes(node));
                        return;
                    }
                    
                }
            }
            
        } else {
            logger.error("{}: unexpected ChildNodes", NodeUtil.getFixedAttributes(node));
        }
        
    }
}
