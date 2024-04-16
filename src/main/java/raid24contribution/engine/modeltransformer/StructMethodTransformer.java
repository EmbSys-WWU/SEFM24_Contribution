package raid24contribution.engine.modeltransformer;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.engine.util.Constants;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCREFERENCETYPE;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.AccessExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.variables.SCClassInstance;

public class StructMethodTransformer implements ModelTransformer {
    
    private static final Logger logger = LogManager.getLogger(StructMethodTransformer.class.getName());
    
    public StructMethodTransformer() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Transforms all structs with methods by transforming the methods to global methods, removing them
     * from the struct and reshaping all calls to the method.
     */
    @Override
    public SCSystem transformModel(SCSystem model) {
        List<SCFunction> globalFunctions = new ArrayList<>();
        List<SCClass> structs = new ArrayList<>();
        // lookup structs
        for (SCClass scclass : model.getClasses()) {
            if (!scclass.isSCModule() && !scclass.isChannel()) {
                globalFunctions.addAll(transformStruct(scclass));
                structs.add(scclass);
            }
        }
        model.addGlobalFunctions(globalFunctions);
        for (SCClass struct : structs) {
            transformAllCallers(struct, model);
        }
        return null;
    }
    
    private List<SCFunction> transformStruct(SCClass struct) {
        assert struct != null;
        ArrayList<SCFunction> transformedFunctions = new ArrayList<>();
        if (!struct.getMemberFunctions().isEmpty()) {
            for (SCFunction function : struct.getMemberFunctions()) {
                transformedFunctions.add(transformFunction(function));
            }
            struct.getMemberFunctions().clear();
        }
        return transformedFunctions;
    }
    
    /**
     * Transforms a struct function by adding the struct reference parameter, renaming it for uniqueness
     * (using underscore as delimiter) and transforming all SCVariableExpressions using struct
     * variables.
     * 
     * @param function
     * @return
     */
    private SCFunction transformFunction(SCFunction function) {
        assert function != null;
        // renaming the function for uniqueness
        function.setName(
                function.getSCClass().getName() + Constants.STRUCT_METHOD_PREFIX_DELIMITER + function.getName());
        function.addParameter(
                new SCParameter(new SCClassInstance("this", function.getSCClass(), null), SCREFERENCETYPE.BYREFERENCE));
        transformInnerExpressions(function);
        return function;
    }
    
    /**
     * Transforms all SCVariableExpressions using struct variables to AccessExpressions using the struct
     * reference parameter this.
     * 
     * @param function
     */
    private void transformInnerExpressions(SCFunction function) {
        List<Pair<Expression, Expression>> replacements = new ArrayList<>();
        for (Expression expr : function.getAllExpressions()) {
            if (expr instanceof SCVariableExpression scVarExpr) {
                SCVariable scVar = scVarExpr.getVar();
                if (!function.getLocalVariablesAndParametersAsSCVars().contains(scVar)) {
                    if (!(scVarExpr.getParent() instanceof AccessExpression)) {
                        if (function.getSCClass().getMemberByName(scVar.getName()) != null) {
                            AccessExpression access =
                                    new AccessExpression(expr.getNode(),
                                            new SCVariableExpression(null,
                                                    new SCClassInstance("this", function.getSCClass(), null)),
                                            ".", scVarExpr);
                            replacements.add(new Pair<>(expr, access));
                        } else {
                            logger.warn(
                                    "found unbound variable {} in former struct function {} that is not a member of the struct",
                                    scVar, function.getName());
                        }
                    }
                }
            }
        }
        function.replaceExpressions(replacements);
    }
    
    /**
     * Transforms all expressions calling a former struct method in the current model.
     * 
     * @param transformedStruct
     * @param model
     */
    private void transformAllCallers(SCClass transformedStruct, SCSystem model) {
        // transform global functions
        for (SCFunction globalFunction : model.getGlobalFunctions()) {
            globalFunction
                    .replaceExpressions(transformFunctionCalls(transformedStruct, globalFunction.getBody(), model));
        }
        // transform member functions
        for (SCClass scclass : model.getClasses()) {
            for (SCFunction memberFunction : scclass.getMemberFunctions()) {
                memberFunction
                        .replaceExpressions(transformFunctionCalls(transformedStruct, memberFunction.getBody(), model));
            }
        }
    }
    
    /**
     * Replacing all structs access function calls with simple global function calls.
     * 
     * @param struct
     * @param body
     * @param model
     * @return
     */
    private List<Pair<Expression, Expression>> transformFunctionCalls(SCClass struct, List<Expression> body,
            SCSystem model) {
        List<Pair<Expression, Expression>> replacements = new ArrayList<>();
        for (Expression expr : body) {
            replacements.addAll(transformExpression(expr, struct, model));
        }
        return replacements;
    }
    
    private List<Pair<Expression, Expression>> transformExpression(Expression expr, SCClass struct, SCSystem model) {
        List<Pair<Expression, Expression>> replacements = new ArrayList<>();
        if (expr instanceof AccessExpression access) {
            if (access.getRight() instanceof FunctionCallExpression
                    && access.getLeft() instanceof SCVariableExpression) {
                SCVariable scVar = ((SCVariableExpression) access.getLeft()).getVar();
                if (scVar.getSClassIfPossible() == struct) {
                    logger.debug("found call to refactored struct: {}", expr);
                    FunctionCallExpression functionCall = (FunctionCallExpression) access.getRight();
                    String name = functionCall.getFunction().getName();
                    SCFunction globalFunction = model.getGlobalFunction(name);
                    if (globalFunction != null) {
                        functionCall.addSingleParameter(new SCVariableExpression(access.getNode(), scVar));
                        // transform function call parameters
                        List<Pair<Expression, Expression>> paramReplace = new ArrayList<>();
                        for (Expression param : functionCall.getParameters()) {
                            paramReplace.addAll(transformExpression(param, struct, model));
                        }
                        functionCall.replaceInnerExpressions(paramReplace);
                        replacements.add(new Pair<>(expr, functionCall));
                    } else {
                        logger.warn("couldn't find global function with name: {}", name);
                    }
                }
                return replacements;
            }
        }
        for (Expression innerExpr : expr.crawlDeeper()) {
            replacements.addAll(transformExpression(innerExpr, struct, model));
        }
        return replacements;
    }
}
