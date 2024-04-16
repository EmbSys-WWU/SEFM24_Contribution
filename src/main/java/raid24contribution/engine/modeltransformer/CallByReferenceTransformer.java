package raid24contribution.engine.modeltransformer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCREFERENCETYPE;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.RefDerefExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.sc_model.variables.SCKnownType;
import raid24contribution.sc_model.variables.SCPointer;

/**
 * This transformer changes all call by reference method invocations and methods that use call by
 * reference parameters to C conform call by reference method calls.
 * 
 * A basic call by reference method like: void foo(int &x) { int a = x; int *p = &x;}
 * 
 * is tranformed into: void foo(int *x) { int a = *x; int *p = x;}
 * 
 * while all parameters of call by reference methods like foo(a); are changed to foo(&a);
 * 
 */
public class CallByReferenceTransformer implements ModelTransformer {
    
    @Override
    public SCSystem transformModel(SCSystem model) {
        List<Pair<SCFunction, List<Integer>>> functionsAndParams = new LinkedList<>();
        for (SCFunction fun : model.getGlobalFunctions()) {
            List<Integer> changedParams = transformFunction(fun);
            if (!changedParams.isEmpty()) {
                functionsAndParams.add(new Pair<>(fun, changedParams));
            }
        }
        
        for (SCClass mod : model.getClasses()) {
            for (SCFunction fun : mod.getMemberFunctions()) {
                List<Integer> changedParams = transformFunction(fun);
                if (!changedParams.isEmpty()) {
                    functionsAndParams.add(new Pair<>(fun, changedParams));
                }
            }
        }
        
        for (SCClassInstance inst : model.getInstances()) {
            if (inst instanceof SCKnownType) {
                SCClass mod = inst.getSCClass();
                for (SCFunction fun : mod.getMemberFunctions()) {
                    List<Integer> changedParams = transformFunction(fun);
                    if (!changedParams.isEmpty()) {
                        functionsAndParams.add(new Pair<>(fun, changedParams));
                    }
                }
            }
        }
        
        transformFunctionCalls(model, functionsAndParams);
        
        return model;
    }
    
    public void transformScClass(SCClass mod, SCSystem model) {
        List<Pair<SCFunction, List<Integer>>> functionsAndParams = new LinkedList<>();
        for (SCFunction fun : mod.getMemberFunctions()) {
            List<Integer> changedParams = transformFunction(fun);
            if (!changedParams.isEmpty()) {
                functionsAndParams.add(new Pair<>(fun, changedParams));
            }
        }
        
        transformFunctionCalls(model, functionsAndParams);
    }
    
    /**
     * Transforms the given function into a function conform to the c style call by reference
     * parameters.
     * 
     * @param function
     * @return true if the function has at least one call by reference parameter which was transformed,
     *         false if not.
     */
    private List<Integer> transformFunction(SCFunction function) {
        List<Pair<SCVariable, SCPointer>> varsToChange = new ArrayList<>();
        List<Integer> changedParamIndices = new ArrayList<>();
        // first, we have to change all by reference parameters to pointers
        for (int i = 0; i < function.getParameters().size(); i++) {
            SCParameter param = function.getParameters().get(i);
            if (param.getRefType() == SCREFERENCETYPE.BYREFERENCE) {
                SCVariable var = param.getVar();
                // create the replace variable, a pointer with the name and type
                // of the by reference variable
                // as this is only a parameter, other information should not be
                // necessary.
                SCPointer varReplace = new SCPointer(var.getName(), var.getType());
                varReplace.setConst(var.isConst());
                varsToChange.add(new Pair<>(var, varReplace));
                changedParamIndices.add(i);
                // change by reference parameters to by value parameters
                // and make them pointers of the same type
                param.setRefType(SCREFERENCETYPE.BYVALUE);
                param.setVar(varReplace);
            }
        }
        
        // replace accesses to variables by pointer dereferencing
        for (Pair<SCVariable, SCPointer> varPair : varsToChange) {
            // generate the Expression with which we want to replace
            SCVariable var = varPair.getFirst();
            SCPointer varReplace = varPair.getSecond();
            
            FunctionCrawler directAccessReplace = generateDirectAccessCrawler(var, varReplace);
            directAccessReplace.start(function);
            
            FunctionCrawler refAccessCrawler = generateRefAccessCrawler(var, varReplace);
            refAccessCrawler.start(function);
            
        }
        
        return changedParamIndices;
    }
    
    /**
     * Generates the crawler to transform all direct accesses to call by reference variables into
     * derefencing accesses to these variables. Statements like a = 42; with a being a by reference
     * parameter are transformed into *a = 42;
     * 
     * @param var
     * @param varReplace
     * @return
     */
    private FunctionCrawler generateDirectAccessCrawler(final SCVariable var, final SCPointer varReplace) {
        return new FunctionCrawler() {
            
            @Override
            protected boolean matches(Expression exp) {
                if (exp instanceof SCVariableExpression varExp) {
                    return varExp.getVar().equals(var);
                } else {
                    return false;
                }
            }
            
            @Override
            protected Expression generateReplacement(Expression exp) {
                // replace the variable expression with a refderef
                // expression containing the replacement pointer
                return new RefDerefExpression(exp.getNode(), new SCVariableExpression(exp.getNode(), varReplace),
                        RefDerefExpression.DEREFERENCING);
            }
            
            @Override
            protected boolean goDeeper(Expression exp) {
                // we can always go deeper, except if we find a
                // refderefexpression containing the variable we want to
                // replace. this case is handled in a different crawler
                if (exp instanceof RefDerefExpression rde) {
                    if (rde.getExpression() instanceof SCVariableExpression) {
                        SCVariableExpression ve = (SCVariableExpression) rde.getExpression();
                        return !ve.getVar().equals(var);
                    }
                }
                return true;
                
            }
        };
    }
    
    /**
     * Generates the crawler to transform all referencing accesses to call by reference variables into
     * direct accesses to these variables. Statements like p = &a; with a being a by reference parameter
     * are transformed into p = a;
     * 
     * @param var
     * @param varReplace
     * @return
     */
    private FunctionCrawler generateRefAccessCrawler(final SCVariable var, final SCPointer varReplace) {
        return new FunctionCrawler() {
            
            @Override
            protected boolean matches(Expression exp) {
                // we are looking for var, being refenced
                if (exp instanceof RefDerefExpression rde) {
                    if (rde.getExpression() instanceof SCVariableExpression) {
                        SCVariableExpression ve = (SCVariableExpression) rde.getExpression();
                        return rde.isReferencing() && ve.getVar().equals(var);
                    }
                }
                
                return false;
            }
            
            @Override
            protected Expression generateReplacement(Expression exp) {
                // replacing the dereferenced var by a direct access to
                // varReplace.
                return new SCVariableExpression(exp.getNode(), varReplace);
            }
        };
    }
    
    /**
     * Transforms the parameters of all functions in the list matching the parameter index specified in
     * the integer list. All parameters are referenced for this.
     * 
     * @param functions
     */
    private void transformFunctionCalls(SCSystem model, List<Pair<SCFunction, List<Integer>>> functionsAndParams) {
        
        List<FunctionCrawler> crawlers = new LinkedList<>();
        // generate all crawlers
        for (Pair<SCFunction, List<Integer>> pair : functionsAndParams) {
            SCFunction fun = pair.getFirst();
            List<Integer> paramIndices = pair.getSecond();
            
            crawlers.add(generateParameterCrawler(fun, paramIndices));
        }
        
        // use the crawlers on all global functions
        for (SCFunction fun : model.getGlobalFunctions()) {
            for (FunctionCrawler crawl : crawlers) {
                crawl.start(fun);
            }
        }
        
        // use the crawlers on all module functions
        for (SCClass mod : model.getClasses()) {
            for (SCFunction fun : mod.getMemberFunctions()) {
                for (FunctionCrawler crawl : crawlers) {
                    crawl.start(fun);
                }
            }
        }
        
    }
    
    /**
     * Generates the crawler to transform all variables used with by-reference parameters into
     * referencing accesses to the variables.
     * 
     * @param fun
     * @param paramIndices
     * @return
     */
    private FunctionCrawler generateParameterCrawler(final SCFunction fun, final List<Integer> paramIndices) {
        
        return new FunctionCrawler() {
            
            @Override
            protected boolean matches(Expression exp) {
                // we are looking for functioncallexpressions to the function
                // fun
                if (exp instanceof FunctionCallExpression fce) {
                    return fce.getFunction().equals(fun);
                }
                
                return false;
            }
            
            @Override
            protected Expression generateReplacement(Expression exp) {
                FunctionCallExpression fce = (FunctionCallExpression) exp;
                List<Expression> oldParams = fce.getParameters();
                
                FunctionCallExpression ret = new FunctionCallExpression(fce.getNode(), fce.getFunction(), null);
                List<Expression> newParams = new LinkedList<>();
                
                // wrapping all matching parameters with a referencing
                for (int i = 0; i < oldParams.size(); i++) {
                    Expression paramExp = oldParams.get(i);
                    if (paramIndices.contains(i)) {
                        // in most cases we can just add a referencing operator
                        // in front of the parameter. But we have to take care
                        // if the parameter is already dereferenced. In this
                        // case we can remove the derefencing operation.
                        if (paramExp instanceof RefDerefExpression rde && ((RefDerefExpression) paramExp).isDerefencing()) {
                            newParams.add(rde.getExpression());
                        } else {
                            newParams.add(new RefDerefExpression(paramExp.getNode(), paramExp,
                                    RefDerefExpression.REFERENCING));
                        }
                    } else {
                        newParams.add(paramExp);
                    }
                }
                ret.setParameters(newParams);
                
                return ret;
            }
        };
    }
}
