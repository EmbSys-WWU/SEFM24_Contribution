package raid24contribution.engine.modeltransformer;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.AccessExpression;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.SCPortSCSocketExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.variables.SCPeq;

public class SocketCallTransformer implements ModelTransformer {
    
    private static final Logger logger = LogManager.getLogger(SocketCallTransformer.class.getName());
    
    public SocketCallTransformer() {}
    
    @Override
    public SCSystem transformModel(SCSystem model) {
        if (model == null) {
            logger.error("model is null");
            return null;
        }
        // get all classes with sockets
        List<SCClass> classesWithSocket = new ArrayList<>();
        for (SCClass scclass : model.getClasses()) {
            if (!scclass.getPortsSockets().isEmpty()) {
                // We are currently not using this functionality
                // socket calls are treated in:
                // sc2uppaal.ClassInstanceTransformer.instantiateAndBind
                // transformClass(scclass);
                classesWithSocket.add(scclass);
            }
            for (SCVariable var : scclass.getMembers()) {
                if (var instanceof SCPeq) {
                    classesWithSocket.add(scclass);
                    break;
                }
            }
        }
        removeSocketFromClasses(classesWithSocket);
        return null;
    }
    
    private void removeSocketFromClasses(List<SCClass> classesWithSocket) {
        assert classesWithSocket != null;
        for (SCClass scclass : classesWithSocket) {
            removeSocketFromConstructor(scclass.getConstructor());
        }
    }
    
    private void removeSocketFromConstructor(SCFunction constructor) {
        assert constructor != null;
        assert constructor.getBody() != null;
        assert constructor.getBody() == constructor.getBody();
        List<Expression> exprToRemove = new ArrayList<>();
        for (Expression expr : constructor.getBody()) {
            if (expr instanceof BinaryExpression
                    && ((BinaryExpression) expr).getLeft() instanceof SCPortSCSocketExpression) {
                // socket = socket("..");
                exprToRemove.add(expr);
            } else if (expr instanceof AccessExpression accessExpr) {
                if (accessExpr.getLeft() instanceof SCPortSCSocketExpression
                        && accessExpr.getRight() instanceof FunctionCallExpression
                        && ((FunctionCallExpression) accessExpr.getRight()).getFunction().getName().equals("bind")) {
                    // socket.bind("..");
                    exprToRemove.add(expr);
                }
            } else if (expr instanceof BinaryExpression
                    && ((BinaryExpression) expr).getLeft() instanceof SCVariableExpression
                    && ((SCVariableExpression) ((BinaryExpression) expr).getLeft()).getVar() instanceof SCPeq) {
                // peq = "peq_name";
                exprToRemove.add(expr);
            }
        }
        // getBody does no defensive copying (see assert)
        constructor.getBody().removeAll(exprToRemove);
    }
}
