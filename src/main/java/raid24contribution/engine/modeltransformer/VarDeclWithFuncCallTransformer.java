package raid24contribution.engine.modeltransformer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCProcess;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.SCVariableDeclarationExpression;

/**
 * Convert 'int a = foo();' to 'int a; a = foo();'
 *
 */
public class VarDeclWithFuncCallTransformer implements ModelTransformer {
    
    @Override
    public SCSystem transformModel(SCSystem model) {
        List<SCFunction> globfunc = model.getGlobalFunctions();
        List<SCClass> classes = model.getClasses();
        
        for (SCFunction func : globfunc) {
            modifyBody(func.getBody());
        }
        
        for (SCClass mod : classes) {
            List<SCFunction> memFunc = mod.getMemberFunctions();
            List<SCProcess> processes = mod.getProcesses();
            
            for (SCFunction func : memFunc) {
                modifyBody(func.getBody());
            }
            for (SCProcess proc : processes) {
                SCFunction func = proc.getFunction();
                modifyBody(func.getBody());
            }
            
        }
        
        return model;
    }
    
    private void modifyBody(List<Expression> body) {
        HashMap<Integer, Expression> inserts = new HashMap<>();
        for (int i = 0; i < body.size(); i++) {
            Expression expr = body.get(i);
            if (expr instanceof SCVariableDeclarationExpression vdexpr) {
                if (vdexpr.getFirstInitialValue() instanceof FunctionCallExpression) {
                    Expression funccall =
                            new BinaryExpression(null, vdexpr.getVariable(), "=", vdexpr.getFirstInitialValue());
                    inserts.put(i + 1, funccall);
                    vdexpr.setInitialValues(new LinkedList<>());
                }
            }
        }
        for (Map.Entry<Integer, Expression> entry : inserts.entrySet()) {
            body.add(entry.getKey(), entry.getValue());
        }
    }
}
