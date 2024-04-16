package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCMODIFIER;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCREFERENCETYPE;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.SCDeltaCountExpression;
import raid24contribution.sc_model.expressions.SCStopExpression;
import raid24contribution.sc_model.expressions.SCTimeStampExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.expressions.UnaryExpression;
import raid24contribution.sc_model.variables.SCSimpleType;

/**
 * Handles all SystemC routines. A SystemC routine is a function of the SystemC Standard (e.g.
 * name()). In this transformer also special handling for specific constructs like "dont_initialize"
 * is done.
 * 
 */

public class SystemCRoutineTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        String val = NodeUtil.getAttributeValueByName(node, "name");
        if (val.equals("dont_initialize")) {
            e.setLastProcessModifier(EnumSet.of(SCMODIFIER.DONTINITIALIZE));
        } else {
            // an other routine
            e.setLastType(new Stack<>());
            e.setLastArgumentList(new LinkedList<>());
            e.setLastParameterList(new LinkedList<>());
            handleChildNodes(node, e);
            SCFunction fct = null;
            
            SCVariable var = null;
            if (e.getLastArgumentList().size() > 0) {
                for (Expression exp : e.getLastArgumentList()) {
                    if (exp instanceof ConstantExpression ce) {
                        var = new SCSimpleType("function_parameter", "not specified", ce, false, false,
                                new ArrayList<>());
                    } else if (exp instanceof SCVariableExpression ve) {
                        var = ve.getVar();
                    } else if (exp instanceof FunctionCallExpression fe) {
                        String retType = fe.getFunction().getReturnType();
                        var = new SCSimpleType("functionCall", retType, false, false, new ArrayList<>());
                    } else if (exp instanceof BinaryExpression be) {
                        // TODO: find out exact Type
                        var = new SCSimpleType("function_parameter", "not specified", be, false, false,
                                new ArrayList<>());
                    } else if (exp instanceof UnaryExpression ue) {
                        // TODO: find out exact Type
                        var = new SCSimpleType("function_parameter", "not specified", ue, false, false,
                                new ArrayList<>());
                    }
                }
                
                if (var != null) {
                    SCParameter param = new SCParameter(var, SCREFERENCETYPE.BYVALUE);
                    e.getLastParameterList().add(param);
                }
                
                List<SCParameter> clone = new ArrayList<>();
                for (SCParameter param : e.getLastParameterList()) {
                    clone.add(param);
                }
                e.getLastParameterList().clear();
                
                if (e.getLastType().isEmpty()) {
                    fct = new SCFunction(val, "void", clone);
                } else {
                    fct = new SCFunction(val, e.getLastType().pop(), clone);
                }
            } else {
                if (e.getLastType().isEmpty()) {
                    fct = new SCFunction(val, "void", new ArrayList<>());
                } else {
                    fct = new SCFunction(val, e.getLastType().pop(), new ArrayList<>());
                }
            }
            List<Expression> clone2 = new ArrayList<>();
            for (Expression exp : e.getLastArgumentList()) {
                clone2.add(exp);
            }
            e.getLastArgumentList().clear();
            
            if (val.equals("sc_time_stamp")) { // special handling for
                                               // sc_time_stamp
                SCTimeStampExpression time = new SCTimeStampExpression(node, "");
                e.getExpressionStack().push(time);
            } else if (val.equals("sc_stop")) { // special handling for
                                                // sc_stop
                SCStopExpression stop = new SCStopExpression(node, "");
                e.getExpressionStack().push(stop);
            } else if (val.equals("sc_delta_count") || val.equals("delta_count")) { // special handling for
                // sc_delta_count
                SCDeltaCountExpression delta = new SCDeltaCountExpression(node, "");
                e.getExpressionStack().push(delta);
            } else {
                FunctionCallExpression fct_cal_exp = new FunctionCallExpression(node, fct, clone2);
                e.getExpressionStack().push(fct_cal_exp);
            }
            
        }
    }
}
