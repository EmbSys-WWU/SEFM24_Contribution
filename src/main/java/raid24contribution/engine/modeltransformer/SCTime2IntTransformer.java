package raid24contribution.engine.modeltransformer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.SCVariableDeclarationExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.variables.SCSimpleType;
import raid24contribution.sc_model.variables.SCTime;

/**
 * Replaces alle sc_time occurens by integers. Note: Assumes, that there is only one unique timeunit
 * throughout the system.
 * 
 */
public class SCTime2IntTransformer implements ModelTransformer {
    
    private static final Logger logger = LogManager.getLogger(SCTime2IntTransformer.class.getName());
    
    private Set<String> timeunits;
    
    @Override
    public SCSystem transformModel(SCSystem model) {
        this.timeunits = new HashSet<>();
        List<SCFunction> globfunc = model.getGlobalFunctions();
        List<SCClass> classes = model.getClasses();
        
        for (SCFunction func : globfunc) {
            replaceInAttributes(func);
            List<Pair<Expression, Expression>> replacements = getReplacements(func.getAllExpressions());
            func.replaceExpressions(replacements);
            
        }
        for (SCClass mod : classes) {
            replaceInMembers(mod);
            List<SCFunction> memFunc = mod.getMemberFunctions();
            for (SCFunction func : memFunc) {
                replaceInAttributes(func);
                List<Pair<Expression, Expression>> replacements = getReplacements(func.getAllExpressions());
                func.replaceExpressions(replacements);
            }
        }
        if (this.timeunits.size() > 1) {
            logger.warn("Found more than 1 timeunit! {}", this.timeunits.toString());
        }
        
        return model;
    }
    
    private void replaceInMembers(SCClass scc) {
        List<Integer> memberIndicesToReplace = new LinkedList<>();
        int i = 0;
        for (SCVariable member : scc.getMembers()) {
            SCTime t = getSCTime(member);
            if (t != null) {
                memberIndicesToReplace.add(i);
            }
            i++;
        }
        for (int j : memberIndicesToReplace) {
            SCVariable repl = getReplacementVariable((SCTime) scc.getMembers().get(j));
            scc.getMembers().remove(j);
            scc.getMembers().add(j, repl);
        }
    }
    
    private void addTimeUnitExpr(Expression expr) {
        this.timeunits.add(expr.toStringNoSem());
    }
    
    private void replaceInAttributes(SCFunction func) {
        for (SCParameter par : func.getParameters()) {
            if (par.getVar() instanceof SCTime) {
                par.setVar(getReplacementVariable((SCTime) par.getVar()));
            }
        }
        List<SCVariable> removableVars = new LinkedList<>();
        List<SCVariable> replacementVars = new LinkedList<>();
        for (SCVariable scvar : func.getLocalVariables()) {
            if (scvar instanceof SCTime) {
                removableVars.add(scvar);
                replacementVars.add(getReplacementVariable((SCTime) scvar));
            }
        }
        func.getLocalVariables().removeAll(removableVars);
        func.getLocalVariables().addAll(replacementVars);
    }
    
    private List<Pair<Expression, Expression>> getReplacements(List<Expression> exprlist) {
        Expression replacement = null;
        List<Pair<Expression, Expression>> replacements = new LinkedList<>();
        for (Expression expr : exprlist) {
            if (expr instanceof SCVariableDeclarationExpression vdexpr) {
                SCTime t = getSCTime(vdexpr);
                if (t != null) {
                    SCVariableExpression scvarExpr = (SCVariableExpression) vdexpr.getVariable();
                    SCVariable scvar = getReplacementVariable(t);
                    scvar.setDeclaration(vdexpr);
                    scvar.setConst(t.isConst());
                    scvarExpr.setVar(scvar);
                    // sc_time t; -> inits.size == 0
                    // sc_time t(1, SC_SEC); -> inits.size == 2
                    // sc_time t = sc_time(); -> inits.size = 1
                    // sc_time t = sc_time(1, SC_SEC); -> inits.size = 1
                    List<Expression> ls = vdexpr.getInitialValues();
                    if (ls.size() > 0) {
                        Expression firstInit = ls.get(0);
                        List<Expression> newInitList = new LinkedList<>();
                        if (ls.size() == 2) {
                            newInitList.add(firstInit);
                            vdexpr.setInitialValues(newInitList);
                            addTimeUnitExpr(ls.get(1));
                        } else if (ls.size() == 1) {
                            FunctionCallExpression fce = (FunctionCallExpression) firstInit;
                            if (fce.getParameters().size() == 0) {
                                vdexpr.setInitialValues(newInitList); // empty
                                                                      // list
                            } else if (fce.getParameters().size() == 2) {
                                newInitList.add(fce.getParameters().get(0));
                                addTimeUnitExpr(fce.getParameters().get(1));
                            }
                        }
                    }
                }
            }
            if (expr instanceof SCVariableExpression varExpr) {
                SCTime t = getSCTime(varExpr);
                if (t != null) {
                    SCVariable scvar = getReplacementVariable(t);
                    replacement = new SCVariableExpression(varExpr.getNode(), scvar);
                    replacements.add(new Pair<>(varExpr, replacement));
                }
            }
            if (expr instanceof FunctionCallExpression fce) {
                if (fce.getFunction().getName().equals("sc_time")) {
                    // sc_time(1, SC_SEC) somewhere in the code
                    if (fce.getParameters().size() == 2) {
                        replacement = fce.getParameters().get(0);
                        replacements.add(new Pair<>(expr, replacement));
                        addTimeUnitExpr(fce.getParameters().get(1));
                    }
                }
            }
        }
        return replacements;
    }
    
    private SCTime getSCTime(SCVariable scvar) {
        if (scvar instanceof SCTime) {
            return (SCTime) scvar;
        }
        return null;
    }
    
    private SCTime getSCTime(SCVariableExpression varExpr) {
        return (getSCTime(varExpr.getVar()));
    }
    
    private SCTime getSCTime(SCVariableDeclarationExpression expr) {
        if (expr.getVariable() instanceof SCVariableExpression) {
            return getSCTime((SCVariableExpression) expr.getVariable());
        }
        return null;
    }
    
    public static SCVariable getReplacementVariable(SCTime t) {
        return new SCSimpleType(t.getName(), "int");
    }
}
