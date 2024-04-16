package raid24contribution.engine.modeltransformer;

import java.util.LinkedList;
import java.util.List;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCProcess;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.EmptyExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.SCVariableDeclarationExpression;

/**
 * Replaces SCVariableDeclarationExpressions (e.g., int i = 42;) in all functions with either
 * binaryExpressions (i, "=" 42) or empty expressions. Useful if the declaration of veriables have
 * to be done statically and therefore must be removed from the body of functions.
 * 
 */
public class VariableDeclarationTransformer implements ModelTransformer {
    
    @Override
    public SCSystem transformModel(SCSystem model) {
        List<SCFunction> globfunc = model.getGlobalFunctions();
        List<SCClass> classes = model.getClasses();
        
        for (SCFunction func : globfunc) {
            List<Pair<Expression, Expression>> replacements = getVDReplaceList(func.getAllExpressions());
            func.replaceExpressions(replacements);
        }
        
        for (SCClass mod : classes) {
            List<SCFunction> memFunc = mod.getMemberFunctions();
            List<SCProcess> processes = mod.getProcesses();
            
            for (SCFunction func : memFunc) {
                List<Pair<Expression, Expression>> replacements = getVDReplaceList(func.getAllExpressions());
                func.replaceExpressions(replacements);
            }
            for (SCProcess proc : processes) {
                SCFunction func = proc.getFunction();
                List<Pair<Expression, Expression>> replacements = getVDReplaceList(func.getAllExpressions());
                func.replaceExpressions(replacements);
            }
            
        }
        
        return model;
    }
    
    private List<Pair<Expression, Expression>> getVDReplaceList(List<Expression> exprlist) {
        List<Pair<Expression, Expression>> replacements = new LinkedList<>();
        for (Expression expr : exprlist) {
            if (expr instanceof SCVariableDeclarationExpression vdexpr) {
                Expression replacement;
                if (vdexpr.getInitialValues().size() != 0) {
                    replacement = new BinaryExpression(vdexpr.getNode(), vdexpr.getVariable(), "=",
                            vdexpr.getFirstInitialValue());
                } else {
                    replacement = new EmptyExpression(vdexpr.getNode());
                }
                
                replacements.add(new Pair<>(vdexpr, replacement));
            }
        }
        
        return replacements;
    }
}
