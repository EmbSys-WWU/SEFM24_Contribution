package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.typetransformer.KnownTypeTransformer;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCPORTSCSOCKETTYPE;
import raid24contribution.sc_model.SCPort;
import raid24contribution.sc_model.SCSocket;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.expressions.SCClassInstanceExpression;
import raid24contribution.sc_model.expressions.SCVariableDeclarationExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.sc_model.expressions.TimeUnitExpression;
import raid24contribution.sc_model.variables.SCArray;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.sc_model.variables.SCEvent;
import raid24contribution.sc_model.variables.SCKnownType;
import raid24contribution.sc_model.variables.SCPeq;
import raid24contribution.sc_model.variables.SCPointer;
import raid24contribution.sc_model.variables.SCSimpleType;
import raid24contribution.sc_model.variables.SCTime;

/**
 * 
 * @author rschroeder
 * 
 */
public class DeclaratorTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(DeclaratorTransformer.class.getName());
    
    private Node n_declarator;
    private Node n_qualified_id;
    private Node n_declarator_suffixes;
    private String varName;
    private String varType;
    boolean isTypedef;
    boolean isStatic;
    boolean isConstant;
    List<String> otherTypeModifiers;
    List<String> templateArguments;
    
    private enum SCOPE {
        FUNCTION, CLASS, SYSTEM
    }
    
    SCOPE scope;
    
    @Override
    public void transformNode(Node node, Environment e) {
        this.n_declarator = findChildNode(node, "declarator");
        this.n_qualified_id = findChildNode(this.n_declarator, "qualified_id");
        this.n_declarator_suffixes = findChildNode(this.n_declarator, "declarator_suffixes");
        
        if (e.getCurrentFunction() != null) {
            this.scope = SCOPE.FUNCTION;
        } else if (e.getClass() != null) {
            this.scope = SCOPE.CLASS;
        } else {
            this.scope = SCOPE.SYSTEM;
        }
        
        handleInitialization(node, e);
        
        setTypeModifiers(node, e);
        setTemplateArguments(node, e);
        
        if (e.getLastType().size() == 0) {
            logger.error("{}: Can't get variable type", NodeUtil.getFixedAttributes(node));
            return;
        }
        this.varType = e.getLastType().lastElement();
        
        if (this.n_qualified_id == null) {
            handlePointer(node, e);
        } else {
            this.varName = NodeUtil.getAttributeValueByName(this.n_qualified_id, "name");
            if (this.varName == null) {
                logger.error("{}: Can't get variable name", NodeUtil.getFixedAttributes(node));
                return;
            } else if (this.isTypedef) {
                handleTypedef(node, e);
            } else if (e.getFoundMemberType().equals("PortSocket")) {
                handlePortSocket(node, e);
            } else if (e.getTransformerFactory().isSimpleType(this.varType)) {
                handleSimpleType(node, e);
            } else if (this.varType.equals("sc_event")) {
                handleSCEvent(node, e);
            } else if (this.varType.equals("sc_time")) {
                handleSCTime(node, e);
            } else if (this.varType.equals("peq_with_cb_and_phase")) {
                handleSCPeq(node, e);
            } else if (!e.getKnownTypes().isEmpty() && e.getKnownTypes().containsKey(this.varType)) {
                handleKnownType(node, e);
            } else if (!e.getClassList().isEmpty() && e.getClassList().containsKey(this.varType)) {
                handleClass(node, e);
            } else if (e.getTransformerFactory().isKnownType(this.varType)) {
                handleUnititializedKnownType(node, e);
            } else {
                handleUnknownClass(node, e);
            }
        }
    }
    
    private void addVariableDeclarationExprWithParameters(Node node, Environment e, SCVariable var,
            List<Expression> parameters) {
        SCVariableDeclarationExpression vde = null;
        Expression classInstanceOrVariableExpr = null;
        if (var instanceof SCClassInstance classInstance) {
            classInstance.setInstanceLabel(checkLabelExistence(parameters));
            classInstanceOrVariableExpr = new SCClassInstanceExpression(node, classInstance);
            e.getSystem().addInstance((SCClassInstance) var);
        } else {
            classInstanceOrVariableExpr = new SCVariableExpression(node, var);
        }
        if (parameters == null || parameters.size() == 0) {
            vde = new SCVariableDeclarationExpression(node, classInstanceOrVariableExpr);
        } else {
            vde = new SCVariableDeclarationExpression(node, classInstanceOrVariableExpr, parameters);
        }
        
        switch (this.scope) {
            case FUNCTION:
                e.getExpressionStack().add(vde);
                e.getCurrentFunction().addLocalVariable(var);
                break;
            case CLASS:
                e.getCurrentClass().addMember(var);
                break;
            case SYSTEM:
                e.getSystem().addGlobalVariable(var);
                break;
        }
    }
    
    private void addVariableAndDeclarationExprIfNeeded(Node node, Environment e, SCVariable var) {
        
        if (e.getLastInitializer() != null) {
            List<Expression> parameters = new ArrayList<>();
            parameters.add(e.getLastInitializer());
            addVariableDeclarationExprWithParameters(node, e, var, parameters);
            e.setLastInitializer(null);
        } else if (e.getLastArgumentList() != null && !e.getLastArgumentList().isEmpty()) {
            addVariableDeclarationExprWithParameters(node, e, var, e.getLastArgumentList());
            e.setLastArgumentList(new LinkedList<>());
        } else {
            addVariableDeclarationExprWithParameters(node, e, var, null);
        }
    }
    
    private SCVariable getSCArray(Node node, Environment e) {
        int dimCount = Integer.valueOf(NodeUtil.getAttributeValueByName(this.n_declarator_suffixes, "arrayCounter"));
        if (dimCount != 1) {
            logger.error("{}: Multidimensional arrays are not supported", NodeUtil.getFixedAttributes(node));
            return null;
        }
        if (this.n_declarator_suffixes.getChildNodes().getLength() != 0) {
            // 'int arr[expr];'
            handleChildNodes(this.n_declarator_suffixes, e);
            if (e.getExpressionStack().size() == 0) {
                logger.error("{}: Can't get array size", NodeUtil.getFixedAttributes(node));
            } else {
                return new SCArray(this.varName, this.varType, e.getExpressionStack().pop());
            }
        } else {
            // 'int arr[] = {...};
            // or even 'int arr[];' :(
            return new SCArray(this.varName, this.varType);
        }
        return null;
    }
    
    private void setTypeModifiers(Node n, Environment e) {
        this.isTypedef = false;
        this.isStatic = false;
        this.isConstant = false;
        this.otherTypeModifiers = new ArrayList<>(0);
        
        for (String modifier : e.getFoundTypeModifiers()) {
            if (modifier.equals("typedef")) {
                // TODO: differenciate between func/class/global?
                this.isTypedef = true;
            } else if (modifier.equals("static")) {
                this.isStatic = true;
            } else if (modifier.equals("const")) {
                this.isConstant = true;
            } else {
                this.otherTypeModifiers.add(modifier);
            }
        }
        e.getFoundTypeModifiers().clear();
    }
    
    private void setTemplateArguments(Node node, Environment e) {
        this.templateArguments = new ArrayList<>(0);
        for (String subtype : e.getLastType_TemplateArguments()) {
            this.templateArguments.add(subtype);
        }
    }
    
    // Checks whether the current parameter list contains any label in the context of class
    // instantiation
    private String checkLabelExistence(List<Expression> parameters) {
        if (parameters != null && !(parameters.isEmpty()) && parameters.get(0) instanceof ConstantExpression) {
            ConstantExpression val = (ConstantExpression) parameters.get(0);
            if (val.getValue().length() >= 2) {
                if (val.getValue().charAt(0) == ('\"')
                        && val.getValue().charAt(val.getValue().length() - 1) == ('\"')) {
                    if (val.getValue().length() == 2) {
                        return "";
                    } else {
                        return val.getValue().substring(1, val.getValue().length() - 1);
                    }
                }
            }
        }
        return null;
    }
    
    private void handleTypedef(Node node, Environment e) {
        // TODO: handle typedefs
        logger.warn("Typedefs are currently ignored: {}", NodeUtil.getFixedAttributes(node));
    }
    
    private void handlePointer(Node node, Environment e) {
        Node ptrOp = findChildNode(this.n_declarator, "ptr_operator");
        if (NodeUtil.getAttributeValueByName(ptrOp, "name").equals("*")) {
            Node n_declarator_declarator = findChildNode(this.n_declarator, "declarator");
            Node n_decl_decl_qualified_id = findChildNode(n_declarator_declarator, "qualified_id");
            this.varName = NodeUtil.getAttributeValueByName(n_decl_decl_qualified_id, "name");
            SCPointer ptr = new SCPointer(this.varName, this.varType, this.isStatic, this.isConstant,
                    this.otherTypeModifiers, e.getLastInitializer());
            addVariableAndDeclarationExprIfNeeded(node, e, ptr);
        } else {
            logger.error("{}: Node does not have an attribute name with the value * and is therefore no Pointer.",
                    NodeUtil.getFixedAttributes(node));
        }
    }
    
    private void handlePortSocket(Node node, Environment e) {
        switch (this.scope) {
            case CLASS:
                SCPORTSCSOCKETTYPE portType = e.getLastPortSocketType();
                SCPort ps =
                        (portType == SCPORTSCSOCKETTYPE.SC_SOCKET) ? new SCSocket(this.varName, this.varType, portType)
                                : new SCPort(this.varName, this.varType, portType);
                e.getCurrentClass().addPortSocket(ps);
                break;
            case FUNCTION:
            case SYSTEM:
                logger.error("{}: SCPortSocket declared outside class", NodeUtil.getFixedAttributes(node));
                break;
        }
    }
    
    private void handleSimpleType(Node node, Environment e) {
        SCVariable var = null;
        if (this.n_declarator_suffixes != null) { // we have an array
            var = getSCArray(node, e);
        } else {
            var = new SCSimpleType(this.varName, this.varType, e.getLastInitializer(), this.isStatic, this.isConstant,
                    this.otherTypeModifiers);
        }
        if (var != null) {
            addVariableAndDeclarationExprIfNeeded(node, e, var);
        } else {
            logger.error("{}: Couldn't get variable", NodeUtil.getFixedAttributes(node));
        }
    }
    
    private void handleSCEvent(Node node, Environment e) {
        switch (this.scope) {
            case CLASS:
                SCEvent se = new SCEvent(this.varName, this.isStatic, this.isConstant, this.otherTypeModifiers);
                e.getCurrentClass().addEvent(se);
                break;
            case FUNCTION:
            case SYSTEM:
                logger.error("{}: SCEvent declared outside class", NodeUtil.getFixedAttributes(node));
                break;
        }
    }
    
    private void handleSCTime(Node node, Environment e) {
        // possible SC_TIME declarations
        // sc_time t;
        // sc_time t(1, SC_SEC);
        // sc_time t = sc_time(1, SC_SEC); // 'initialized'
        SCTime st = null;
        
        List<Expression> parameters = e.getLastArgumentList();
        
        boolean useFuncCall = false;
        if (e.getLastInitializer() != null && e.getLastInitializer() instanceof FunctionCallExpression) {
            FunctionCallExpression fce = (FunctionCallExpression) e.getLastInitializer();
            parameters = fce.getParameters();
            useFuncCall = true;
        }
        
        st = new SCTime(this.varName, this.isStatic, this.isConstant, this.otherTypeModifiers, useFuncCall);
        
        if (parameters.size() == 2) {
            // the sc_time variable is initialized with a value and a timeunit
            // (e.g., 1, SC_NS).
            Expression timeunit = parameters.get(1);
            if (!(timeunit instanceof TimeUnitExpression)) {
                logger.error(
                        "{}: Encountered unexpexted second parameter for sc_time initilization. Expected was a systemc time unit (e.g. SC_SEC or SC_NS) but got {}",
                        NodeUtil.getFixedAttributes(node), timeunit.toString());
            } else {
                addVariableDeclarationExprWithParameters(node, e, st, parameters);
            }
        } else if (parameters.size() == 0) {
            // the variable is not initialized
            addVariableAndDeclarationExprIfNeeded(node, e, st);
        } else {
            logger.error(
                    "{}: Encountered unexpected argument size for initialization of sc_time. Expected was either none or two parameters but got {}",
                    NodeUtil.getFixedAttributes(node), parameters.size());
        }
    }
    
    private void handleSCPeq(Node node, Environment e) {
        switch (this.scope) {
            case CLASS:
                e.getLastType_TemplateArguments().clear();
                SCPeq peq = new SCPeq(this.varName, e.getCurrentClass(), this.templateArguments, this.isStatic,
                        this.isConstant, this.otherTypeModifiers);
                e.getCurrentClass().addMember(peq);
                break;
            case FUNCTION:
            case SYSTEM:
                logger.error("{}: SCPeq declared outside class", NodeUtil.getFixedAttributes(node));
                break;
        }
    }
    
    private void handleKnownType(Node node, Environment e) {
        KnownTypeTransformer typeTrans = e.getTransformerFactory().getTypeTransformer(this.varType, e);
        if (typeTrans != null) {
            SCKnownType kt = null;
            switch (this.scope) {
                case FUNCTION:
                    kt = typeTrans.initiateInstance(this.varName, e.getLastArgumentList(), e, this.isStatic,
                            this.isConstant, this.otherTypeModifiers);
                    break;
                case CLASS:
                    kt = typeTrans.createInstance(this.varName, e, this.isStatic, this.isConstant,
                            this.otherTypeModifiers);
                    break;
                case SYSTEM:
                    logger.error("{}: unimplemented handling of global var of known type",
                            NodeUtil.getFixedAttributes(node));
                    break;
            }
            if (kt != null) {
                addVariableAndDeclarationExprIfNeeded(node, e, kt);
            } else {
                logger.error("{}: Could not create known type", NodeUtil.getFixedAttributes(node));
            }
        } else {
            logger.error("{}: Could not get known type transformer", NodeUtil.getFixedAttributes(node));
        }
    }
    
    private void handleClass(Node node, Environment e) {
        SCVariable var = null;
        switch (this.scope) {
            case CLASS:
            case FUNCTION:
                if (this.n_declarator_suffixes == null) {
                    var = new SCClassInstance(this.varName, e.getClassList().get(this.varType), e.getCurrentClass());
                } else if (NodeUtil.containsAttribute(this.n_declarator_suffixes, "arrayCounter")) {
                    // we have a struct array here
                    var = getSCArray(node, e);
                    ((SCArray) var).setIsArrayOfSCClassInstances(e.getClassList().get(this.varType));
                } else {
                    logger.error(
                            "{}: Encountered a module variable with name {} which seems to be something like a struct but is neither a normal struct nor a struct array.",
                            NodeUtil.getFixedAttributes(this.n_declarator_suffixes), this.varName);
                }
                break;
            case SYSTEM:
                var = new SCClassInstance(this.varName, e.getClassList().get(this.varType), e.getCurrentClass());
                break;
        }
        if (var != null) {
            addVariableAndDeclarationExprIfNeeded(node, e, var);
        } else {
            logger.error("{}: Can't handle class correctly", NodeUtil.getFixedAttributes(node));
        }
    }
    
    /**
     * Handles all possible kinds of initialization which can occur during declaration. There are three
     * cases: <br>
     * (1) by initializer, e.g., int x = 43; sc_time t = sc_time(2, SC_NS); <br>
     * (2) by argument list, e.g., MyModule mod("module", ...) <br>
     * (3) no initialization, e.g., int j;
     * 
     * @param node
     * @param e
     */
    void handleInitialization(Node node, Environment e) {
        // case 1 and two are mutual exclusive, so we don't care for order of
        // the check
        if (findChildNode(node, "initializer") != null) {
            // case 1
            e.setLastInitializer(null);
            // also sets e.lastInitializer to the found initializer
            handleNode(findChildNode(node, "initializer"), e);
        }
        if (findChildNode(node, "arguments_list") != null) {
            e.setLastArgumentList(null);
            // also sets e.lastArgumentList to the found argument list
            handleNode(findChildNode(node, "arguments_list"), e);
        }
        // do nothing in case 3.
    }
    
    private void handleUnititializedKnownType(Node node, Environment e) {
        handleKnownType(node, e);
    }
    
    private void handleUnknownClass(Node node, Environment e) {
        SCClass s = new SCClass(this.varType);
        e.getClassList().put(this.varType, s);
        SCClassInstance si = new SCClassInstance(this.varName, s, e.getCurrentClass());
        addVariableAndDeclarationExprIfNeeded(node, e, si);
    }
    
}
