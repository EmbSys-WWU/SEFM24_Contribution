package raid24contribution.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.w3c.dom.Node;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCMODIFIER;
import raid24contribution.sc_model.SCPORTSCSOCKETTYPE;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCPort;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.variables.SCEvent;

public class Environment {
    
    /**
     * this is the system, with it's ModuleInstances, global Variables and global Functions //it
     * contains also the Portbindings between the ModuleInstances
     **/
    private SCSystem system;
    
    private final TransformerFactory transformerFactory = new TransformerFactory();
    
    /** this String is used for the detection of a constructor or the SCMain **/
    private String location = "";
    
    /**
     * this Map contains all classes which are used in the System
     */
    private HashMap<String, SCClass> classList = null;
    /**
     * this Map contains all KnownTypes which are used in the System
     */
    private HashMap<String, SCClass> knownTypes = null;
    
    /**
     * refers to the class currently under transformation.
     */
    private SCClass currentClass = null;
    
    /**
     * refers to the current Function whose XML-Tags will be transform
     */
    private SCFunction currentFunction = null;
    
    /**
     * refers to the current port or socket we encountered in the xml document. This is needed to
     * determine the port/socket on which a function is invoked.
     */
    private SCPort currentPortSocket = null;
    
    /**
     * save the Type of the current Variable, if its an empty String its a Variable otherwise its a port
     * or socket
     */
    private String foundMemberType = "";
    /**
     * saves the last Type of a Port or a socket
     */
    private SCPORTSCSOCKETTYPE lastPortSocketType = null;
    
    /**
     * contains the types which have been found, should be empty if we are outside a MemberDeclaration
     * or an InitDeclaration
     */
    private Stack<String> lastType = null;
    /**
     * contains the found Modifiers of the current Variable, such as static or const
     */
    private Stack<String> foundTypeModifiers = null;
    /**
     * contains the arguments of the current Variable
     */
    private List<String> lastType_TemplateArguments = null;
    /**
     * contains the last Id, this can be nearly everything, for example: a Variable, a Module, a Struct,
     * ...
     */
    private Stack<String> lastQualifiedId = null;
    /**
     * this contains the Parameter of the currently found function
     */
    private List<SCParameter> lastParameterList = null;
    /**
     * this contains the Call-Arguments of the current Fucntioncall
     */
    private List<Expression> lastArgumentList = null;
    
    /**
     * during phase 1, in this map, all functionbodys will be saved, after phase 6, this Map should be
     * empty
     */
    private HashMap<String, HashMap<String, List<Node>>> functionBodys = null;
    /**
     * this contains the FunctionBody of the current Function
     */
    private List<Node> lastFunctionBody = null;
    /**
     * this saves the actual AccessKey, public or private
     */
    private String CurrentAccessKey = "";
    /**
     * during the functionparsing the found Expressions will be saved in this stack, if we reach the end
     * of the highest BlockStatementTag we put the Expressions from this stack in the current function
     */
    private Stack<Expression> ExpressionStack = null;
    /**
     * this refers to the Function which will be marked as a Process in the corresponding XML-Tag
     */
    private SCFunction lastProcessFunction = null;
    /**
     * this is the Name of the Process but nearly everytime its the functionname
     */
    private String lastProcessName = "";
    /**
     * this list contains the Events where the Process is sensitive for
     */
    private List<SCEvent> sensitivityList;
    /**
     * this contains the modifiers of the process, for example dont_initialize
     */
    private EnumSet<SCMODIFIER> lastProcessModifier = null;
    
    /**
     * this marks weather we are in the highest BlockStatement(false) or we are in a BlockStatement in a
     * Blockstatement etc. (true)
     */
    private boolean rekursiveBlockStatement = false;
    /**
     * this marks weather we are in a functionblock or not
     */
    private boolean FunctionBlock = true;
    /**
     * this marks if we build the system or not
     */
    private boolean systemBuilding = false;
    /**
     * this contains the last Initializer which was found
     */
    private Expression lastInitializer = null;
    /**
     * this contains the last scope-overwrite
     */
    private String lastScopeOverwrite = "";
    /**
     * this marks weather we are in a constructor or not
     */
    private boolean inConstructor = false;
    
    public Environment() {
        this.lastType = new Stack<>();
        this.lastQualifiedId = new Stack<>();
        this.ExpressionStack = new Stack<>();
        this.functionBodys = new HashMap<>();
        this.classList = new HashMap<>();
        this.lastParameterList = new ArrayList<>();
        this.lastArgumentList = new ArrayList<>();
        this.sensitivityList = new ArrayList<>();
        this.knownTypes = new HashMap<>();
        this.foundTypeModifiers = new Stack<>();
        this.lastType_TemplateArguments = new ArrayList<>();
        this.system = new SCSystem();
        try {
            this.transformerFactory.initialize();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public SCSystem getSystem() {
        return this.system;
    }
    
    public void setSystem(SCSystem system) {
        this.system = system;
    }
    
    public String getLocation() {
        return this.location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    /**
     * Returns a map of class names, mapped to their corresponding implementations.
     * 
     * @return
     */
    public HashMap<String, SCClass> getClassList() {
        return this.classList;
    }
    
    public void setClassList(HashMap<String, SCClass> structList) {
        this.classList = structList;
    }
    
    public HashMap<String, SCClass> getKnownTypes() {
        return this.knownTypes;
    }
    
    public void setKnownTypes(HashMap<String, SCClass> knownTypes) {
        this.knownTypes = knownTypes;
    }
    
    public SCClass getCurrentClass() {
        return this.currentClass;
    }
    
    public void setCurrentClass(SCClass scclass) {
        this.currentClass = scclass;
    }
    
    public SCFunction getCurrentFunction() {
        return this.currentFunction;
    }
    
    public void setCurrentFunction(SCFunction currentFunction) {
        this.currentFunction = currentFunction;
    }
    
    public SCPort getCurrentPortSocket() {
        return this.currentPortSocket;
    }
    
    public void setCurrentPortSocket(SCPort currentPortSocket) {
        this.currentPortSocket = currentPortSocket;
    }
    
    public String getFoundMemberType() {
        return this.foundMemberType;
    }
    
    public void setFoundMemberType(String foundMemberType) {
        this.foundMemberType = foundMemberType;
    }
    
    public SCPORTSCSOCKETTYPE getLastPortSocketType() {
        return this.lastPortSocketType;
    }
    
    public void setLastPortSocketType(SCPORTSCSOCKETTYPE lastPortSocketType) {
        this.lastPortSocketType = lastPortSocketType;
    }
    
    public Stack<String> getLastType() {
        return this.lastType;
    }
    
    public void setLastType(Stack<String> lastType) {
        this.lastType = lastType;
    }
    
    public Stack<String> getFoundTypeModifiers() {
        return this.foundTypeModifiers;
    }
    
    public void setFoundTypeModifiers(Stack<String> foundTypeModifiers) {
        this.foundTypeModifiers = foundTypeModifiers;
    }
    
    public List<String> getLastType_TemplateArguments() {
        return this.lastType_TemplateArguments;
    }
    
    public void setLastType_TemplateArguments(List<String> lastType_TemplateArguments) {
        this.lastType_TemplateArguments = lastType_TemplateArguments;
    }
    
    public Stack<String> getLastQualifiedId() {
        return this.lastQualifiedId;
    }
    
    public void setLastQualifiedId(Stack<String> lastQualifiedId) {
        this.lastQualifiedId = lastQualifiedId;
    }
    
    public List<SCParameter> getLastParameterList() {
        return this.lastParameterList;
    }
    
    public void setLastParameterList(List<SCParameter> lastParameterList) {
        this.lastParameterList = lastParameterList;
    }
    
    public List<Expression> getLastArgumentList() {
        return this.lastArgumentList;
    }
    
    public void setLastArgumentList(List<Expression> lastArgumentList) {
        this.lastArgumentList = lastArgumentList;
    }
    
    public HashMap<String, HashMap<String, List<Node>>> getFunctionBodys() {
        return this.functionBodys;
    }
    
    public void setFunctionBodys(HashMap<String, HashMap<String, List<Node>>> functionBodys) {
        this.functionBodys = functionBodys;
    }
    
    public List<Node> getLastFunctionBody() {
        return this.lastFunctionBody;
    }
    
    public void setLastFunctionBody(List<Node> lastFunctionBody) {
        this.lastFunctionBody = lastFunctionBody;
    }
    
    public String getCurrentAccessKey() {
        return this.CurrentAccessKey;
    }
    
    public void setCurrentAccessKey(String currentAccessKey) {
        this.CurrentAccessKey = currentAccessKey;
    }
    
    public Stack<Expression> getExpressionStack() {
        return this.ExpressionStack;
    }
    
    public void setExpressionStack(Stack<Expression> expressionStack) {
        this.ExpressionStack = expressionStack;
    }
    
    public SCFunction getLastProcessFunction() {
        return this.lastProcessFunction;
    }
    
    public void setLastProcessFunction(SCFunction lastProcessFunction) {
        this.lastProcessFunction = lastProcessFunction;
    }
    
    public String getLastProcessName() {
        return this.lastProcessName;
    }
    
    public void setLastProcessName(String lastProcessName) {
        this.lastProcessName = lastProcessName;
    }
    
    public List<SCEvent> getSensitivityList() {
        return this.sensitivityList;
    }
    
    public void setSensitivityList(List<SCEvent> sensitivityList) {
        this.sensitivityList = sensitivityList;
    }
    
    public EnumSet<SCMODIFIER> getLastProcessModifier() {
        return this.lastProcessModifier;
    }
    
    public void setLastProcessModifier(EnumSet<SCMODIFIER> lastProcessModifier) {
        this.lastProcessModifier = lastProcessModifier;
    }
    
    public boolean isRekursiveBlockStatement() {
        return this.rekursiveBlockStatement;
    }
    
    public void setRekursiveBlockStatement(boolean rekursiveBlockStatement) {
        this.rekursiveBlockStatement = rekursiveBlockStatement;
    }
    
    public boolean isFunctionBlock() {
        return this.FunctionBlock;
    }
    
    public void setFunctionBlock(boolean functionBlock) {
        this.FunctionBlock = functionBlock;
    }
    
    public boolean isSystemBuilding() {
        return this.systemBuilding;
    }
    
    public void setSystemBuilding(boolean systemBuilding) {
        this.systemBuilding = systemBuilding;
    }
    
    public Expression getLastInitializer() {
        return this.lastInitializer;
    }
    
    public void setLastInitializer(Expression lastInitializer) {
        this.lastInitializer = lastInitializer;
    }
    
    public String getLastScopeOverwrite() {
        return this.lastScopeOverwrite;
    }
    
    public void setLastScopeOverwrite(String lastScopeOverwrite) {
        this.lastScopeOverwrite = lastScopeOverwrite;
    }
    
    public boolean isInConstructor() {
        return this.inConstructor;
    }
    
    public void setInConstructor(boolean inConstructor) {
        this.inConstructor = inConstructor;
    }
    
    /**
     * Integrates the submitted environment into this environment. This method can be used to
     * conveniently add the results of the parsing of an implementation file into the main environment
     * used the whole system. All modules and structs in e are also added to the known types of this
     * environment.
     * 
     * @param e
     */
    public void integrate(Environment e) {
        // we have to set all modules and structs we integrate into the
        // environment to external as they are already finished.
        for (SCClass struct : e.classList.values()) {
            struct.setExternal(true);
            if (!this.classList.containsKey(struct.getName())) {
                this.classList.put(struct.getName(), struct);
            }
            if (!this.knownTypes.containsKey(struct.getName())) {
                this.knownTypes.put(struct.getName(), struct);
            }
        }
    }
    
    
    public TransformerFactory getTransformerFactory() {
        return this.transformerFactory;
    }
    
    
    
}
