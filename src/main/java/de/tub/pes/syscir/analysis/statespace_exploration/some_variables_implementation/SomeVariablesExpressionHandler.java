package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import static de.tub.pes.syscir.analysis.util.WrapperUtil.wrap;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformationKey;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.SmallStepResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableHolder;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCPortEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

/**
 * Class for the special treatment of some expressions by {@link SomeVariablesProcess} and
 * {@link SomeVariablesScheduler}.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <LocalStateT> the type of local state
 * @param <ValueT> the type of abstracted value
 * @param <TransitionResultT> the type of transition result
 */
public class SomeVariablesExpressionHandler<LocalStateT extends LocalState<LocalStateT, ValueT> & VariableHolder<LocalVariable<?>, ValueT>, ValueT extends AbstractedValue<ValueT, ?, ?>, TransitionResultT extends TransitionResult<TransitionResultT, SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>>, InfoT extends ComposableTransitionInformation<InfoT>> {

    public static class AccessedVariablesInformation extends LinkedHashSet<Variable<?, ?>>
    implements StateInformation<AccessedVariablesInformation> {

        private static final long serialVersionUID = -8549558425124611127L;

        public AccessedVariablesInformation() {
            super();
        }

        public AccessedVariablesInformation(Collection<? extends Variable<?, ?>> copyOf) {
            super(copyOf);
        }

        @Override
        public AccessedVariablesInformation copy() {
            return new AccessedVariablesInformation(this);
        }
    }

    public static final StateInformationKey<AccessedVariablesInformation> VARIABLES_READ_KEY =
            new StateInformationKey<>();
    public static final StateInformationKey<AccessedVariablesInformation> VARIABLES_WRITTEN_KEY =
            new StateInformationKey<>();

    /**
     * Returns the internally used SCEvent for an SCPortEvent and the specific port instance.
     *
     * @param event the SCPortEvent
     * @param instance the actual instance of the channel interfaced by the port
     * @return the internally used SCEvent
     */
    public static SCEvent getPortEvent(String eventType, WrappedSCClassInstance instance) {
        if (instance.getType().startsWith("sc_signal")) {
            if (eventType.equals("default_event") || eventType.equals("change")) {
                return new SCEvent("change", false, false, List.of());
            }
            throw new NoSuchElementException(eventType + " on " + instance.toString());
        } else if (instance.getType().startsWith("sc_fifo")) {
            if (eventType.equals("data_read")) {
                return new SCEvent("data_read_event", false, false, List.of());
            } else if (eventType.equals("data_written")) {
                return new SCEvent("data_written_event", false, false, List.of());
            } else if (eventType.equals("default_event")) {
                throw new NoSuchElementException("fifo has no default_event");
            }
            throw new NoSuchElementException(eventType + " on " + instance.toString());
        } else if (instance.getType().equals("sc_clock")) {
            if (eventType.equals("default_event") || eventType.equals("change")) {
                return new SCEvent("change", false, false, List.of());
            }
            if (eventType.equals("edge")) {
                return new SCEvent("edge", false, false, List.of());
            }
            throw new NoSuchElementException(eventType + " on " + instance.toString());
        }

        throw new UnsupportedOperationException(eventType + " on " + instance.toString());
    }

    private ExpressionCrawler<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, LocalStateT, TransitionResultT, ValueT, InfoT, SomeVariablesProcess<ValueT, InfoT>> crawler;

    private Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition;
    private Predicate<? super LocalVariable<?>> localVariableStorageCondition;

    /**
     * Constructs a new SomeVariableNoInformationExpressionHandler.
     * 
     * @param crawler the expression crawler delegating some calls to this handler
     * @param globalVariableStorageCondition the condition under which values of global variables are
     *        stored
     * @param localVariableStorageCondition the condition under which values of local variables are
     *        stored
     */
    public SomeVariablesExpressionHandler(
            ExpressionCrawler<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, LocalStateT, TransitionResultT, ValueT, InfoT, SomeVariablesProcess<ValueT, InfoT>> crawler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        this.crawler = crawler;
        this.globalVariableStorageCondition = globalVariableStorageCondition;
        this.localVariableStorageCondition = localVariableStorageCondition;
    }

    /**
     * Returns, as an abstracted value, the actual {@link Event} instance of an SCPortEvent, given some
     * global state.
     *
     * @param globalState the global state
     * @param portEvent an SCPortEvent
     * @return the actual {@link Event} instance
     */
    protected GlobalVariable<WrappedSCClassInstance, SCVariable> getEventVariable(SCPort port) {
        WrappedSCClassInstance instance = this.crawler.getChannel(port);
        SCVariable scVariable = getPortEvent("default_event", instance);

        return new GlobalVariable<>(instance, scVariable);
    }

    /**
     * Returns, as an abstracted value, the actual {@link Event} instance of an SCPortEvent, given some
     * global state.
     *
     * @param globalState the global state
     * @param portEvent an SCPortEvent
     * @return the actual {@link Event} instance
     */
    protected GlobalVariable<WrappedSCClassInstance, SCVariable> getEventVariable(SCPortEvent portEvent) {
        WrappedSCClassInstance instance = this.crawler.getChannel(portEvent.getPort());
        SCVariable scVariable = getPortEvent(portEvent.getEventType(), instance);

        return new GlobalVariable<>(instance, scVariable);
    }

    /**
     * Returns, as an abstracted value, the actual {@link Event} instance of an SCPortEvent, given some
     * global state.
     *
     * @param globalState the global state
     * @param portEvent an SCPortEvent
     * @return the actual {@link Event} instance
     */
    protected ValueT getEventValue(SomeVariablesGlobalState<ValueT> globalState,
            GlobalVariable<WrappedSCClassInstance, SCVariable> eventVariable) {
        ValueT eventValue =
                globalState.getValue(eventVariable, this.crawler::getNonDeterminedValue);
        return eventValue;
    }

    /**
     * See
     * {@link ExpressionCrawler#handleSpecialExpression(TransitionResult, LocalState, Expression, int)}.
     * 
     * This implementation overrides the behavior for {@link SCVariableExpression}s and
     * {@link SCPortSCSocketExpression}s by finding the appropriate variable and using it or its stored
     * value as the evaluation result.
     *
     * @param currentState the current state of the evaluation
     * @param expression the next expression to be evaluated
     * @param comingFrom from where the expression is reached (-1 if from its parent, or the index of
     *        the child which has last been evaluated)
     * @return the result overwriting the usual logic of {@link #makeSmallStep(TransitionResult)}, or
     *         null if no special treatment is given
     */
    protected SmallStepResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, TransitionResultT> handleSpecialExpression(
            TransitionResultT currentState, LocalStateT localState, Expression expression, int comingFrom) {
        localState.setStateInformation(VARIABLES_READ_KEY, null);
        localState.setStateInformation(VARIABLES_WRITTEN_KEY, null);

        Object var;
        boolean global;

        if (expression instanceof SCVariableExpression ve) {
            var = ve.getVar();
            if (localState.getTopOfStack().getFunction().getParameters().stream()
                    .anyMatch(p -> p.getVar().equals(var))) {
                global = false;
            } else {
                global = (ve.getVar().getDeclaration() == null) || (ve.getVar().getDeclaration().getParent() == null);
            }
        } else if (expression instanceof SCPortSCSocketExpression pe) {
            var = pe.getSCPortSCSocket();
            global = true;
        } else {
            return null;
        }

        AccessedVariablesInformation readVariables = new AccessedVariablesInformation();
        localState.setStateInformation(VARIABLES_READ_KEY, readVariables);

        assert comingFrom == -1;

        // most variable expressions are treated as their values, but if they appear on the left hand side
        // of an assignment (or they are the rightmost part of an access which appears on the left hand side
        // of an assignment), they are treated as the variables
        boolean treatAsValue;
        if (expression.getParent() instanceof BinaryExpression parent && parent.getOp().equals("=")
                && expression == parent.getLeft()) {
            treatAsValue = false;
        } else if (expression.getParent() instanceof AccessExpression parent) {
            if (expression == parent.getLeft()) {
                treatAsValue = true;
            } else if (parent.getParent() instanceof BinaryExpression grandparent && grandparent.getOp().equals("=")
                    && parent == grandparent.getLeft()) {
                treatAsValue = false;
            } else {
                treatAsValue = true;
            }
        } else if (expression.getParent() instanceof SCVariableDeclarationExpression parent
                && expression == parent.getVariable()) {
            treatAsValue = false;
        } else {
            treatAsValue = true;
        }

        ValueT result;
        if (treatAsValue) {
            // distinguish between local and global variable based on the existence of a DeclarationExpression
            if (!global) {
                LocalVariable<?> variable = new LocalVariable<>(localState.getStackTrace(), var);
                readVariables.add(variable);
                result = localState.getValue(variable, this.crawler::getNonDeterminedValue);
            } else {
                // TODO: assume variable is global, is this correct?
                if (var instanceof SCPortEvent portEvent) {
                    GlobalVariable<WrappedSCClassInstance, SCVariable> variable = getEventVariable(portEvent);
                    readVariables.add(variable);
                    result = getEventValue(currentState.globalState(), variable);
                } else if (var instanceof SCPort port && expression.getParent() instanceof FunctionCallExpression fe
                        && fe.getFunction().getName().equals("wait")) {
                    GlobalVariable<WrappedSCClassInstance, SCVariable> variable = getEventVariable(port);
                    readVariables.add(variable);
                    result = getEventValue(currentState.globalState(), variable);
                } else {
                    ValueT scope = getVariableQualifier(currentState, localState, expression);
                    if (!scope.isDetermined()) {
                        readVariables.add(new GlobalVariable<>(scope, var));
                        this.crawler.returnToParent(expression.getParent(), localState,
                                this.crawler.getNonDeterminedValue());
                        return new SmallStepResult<>(List.of(currentState), false, false);
                    } else {
                        GlobalVariable<?, ?> variable = new GlobalVariable<>(scope.get(), var);
                        readVariables.add(variable);
                        result = currentState.globalState().getVariableValues().getOrDefault(variable,
                                this.crawler.getNonDeterminedValue());
                    }
                }
            }
        } else {
            // see above regarding global/local
            if (!global) {
                result = this.crawler.getDeterminedValue(new LocalVariable<>(localState.getStackTrace(), var));
            } else {
                ValueT scope = getVariableQualifier(currentState, localState, expression);
                if (!scope.isDetermined()) {
                    this.crawler.returnToParent(expression.getParent(), localState,
                            this.crawler.getNonDeterminedValue());
                    return new SmallStepResult<>(List.of(currentState), false, false);
                } else {
                    result = this.crawler.getDeterminedValue(new GlobalVariable<>(scope.get(), var));
                }
            }
        }

        this.crawler.returnToParent(expression.getParent(), localState, result);
        return this.crawler.createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Returns, as an abstracted value, the qualifier for the variable accessed in the given expression.
     * 
     * See {@link Variable}.
     *
     * @param currentState the current state
     * @param localState the local portion of the state
     * @param expression an expression constituting a variable
     * @return the qualifier for the variable
     */
    protected ValueT getVariableQualifier(TransitionResultT currentState, LocalStateT localState, Expression expression) {
        // if the variable is the right hand side of an access, the result of the left hand side is the
        // scope. otherwise, this is the scope.
        if (expression.getParent() instanceof AccessExpression parent && parent.getRight() == expression) {
            ValueT scopeVal = this.crawler.getValueOfExpression(currentState, localState, 1, 0);
            return scopeVal;
        } else {
            return localState.getTopOfStack().getThisValue();
        }
    }

    /**
     * See {@link ExpressionCrawler#aggregateExpressionValue(TransitionResult, LocalState, Expression)}.
     *
     * @param currentState the current state of the evaluation
     * @param localState the local portion of the state
     * @param expression the expression to be evaluated
     * @return an abstraction of the value of the expression (may not be null, but may be non-determined
     *         or an abstraction of null)
     */
    protected ValueT aggregateExpressionValue(TransitionResultT currentState, LocalStateT localState, Expression expression) {
        if (expression instanceof UnaryExpression ue) {
            ValueT param = this.crawler.getValueOfChild(currentState, localState, 0);
            if (!param.isDetermined()) {
                return this.crawler.getNonDeterminedValue();
            }

            if (ue.getOperator().equals("-")) {
                return this.crawler.getDeterminedValue(-1 * this.crawler.getAsInteger(param).get());
            } else if (ue.getOperator().equals("!")) {
                return this.crawler.getDeterminedValue(!this.crawler.getAsBoolean(param).get());
            } else {
                // TODO: add more operators
                return this.crawler.getNonDeterminedValue();
            }
        }

        BinaryOperator<ValueT> simpleOperator;
        if (expression instanceof BinaryExpression be) {
            ValueT left = this.crawler.getValueOfChild(currentState, localState, 0);
            ValueT right = this.crawler.getValueOfChild(currentState, localState, 1);

            if (be.getOp().equals("=")) {
                if (!left.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }

                AccessedVariablesInformation writtenVariables = new AccessedVariablesInformation();
                localState.setStateInformation(VARIABLES_WRITTEN_KEY, writtenVariables);

                if (left.get() instanceof GlobalVariable<?, ?> gv) {
                    writtenVariables.add(gv);
                    if (this.globalVariableStorageCondition.test(gv)) {
                        currentState.globalState().setVariableValue(gv, right);
                    }
                } else if (left.get() instanceof LocalVariable<?> lv) {
                    writtenVariables.add(lv);
                    if (this.localVariableStorageCondition.test(lv)) {
                        localState.setVariableValue(lv, right);
                    }
                }

                return right;
            } else if ((simpleOperator = getSimpleOperator(be.getOp())) != null) {
                return simpleOperator.apply(left, right);
            } else {
                // TODO: add more operators, at least +=, -=, etc.
                return this.crawler.getNonDeterminedValue();
            }
        }

        if (expression instanceof AccessExpression ae) {
            ValueT left = this.crawler.getValueOfChild(currentState, localState, 0);
            ValueT right = this.crawler.getValueOfChild(currentState, localState, 1);

            if (ae.getRight() instanceof FunctionCallExpression fe) {
                return right;
            }

            if (!left.isDetermined() || !right.isDetermined()) {
                return this.crawler.getNonDeterminedValue();
            }

            // TODO: is that right?
            // TODO: always treat VariableExpression as variable if in access and only evaluate in aggregation?
            return currentState.globalState().getVariableValues()
                    .getOrDefault(new GlobalVariable<>(left.get(), (SCVariable) right.get()),
                            this.crawler.getNonDeterminedValue());
        }

        if (expression instanceof SCVariableDeclarationExpression de) {
            if (de.getInitialValues().isEmpty()) {
                return this.crawler.getNonDeterminedValue();
            }

            ValueT left = this.crawler.getValueOfChild(currentState, localState, 0);
            ValueT right = this.crawler.getValueOfChild(currentState, localState, 1);

            if (!left.isDetermined()) {
                return this.crawler.getNonDeterminedValue();
            }

            AccessedVariablesInformation writtenVariables = new AccessedVariablesInformation();
            localState.setStateInformation(VARIABLES_WRITTEN_KEY, writtenVariables);

            if (left.get() instanceof LocalVariable<?> lv) {
                writtenVariables.add(lv);
                if (this.localVariableStorageCondition.test(lv)) {
                    localState.setVariableValue(lv, right);
                }
            }

            if (!right.isDetermined()) {
                return this.crawler.getNonDeterminedValue();
            }

            return right;
        }

        return this.crawler.getNonDeterminedValue();
    }

    protected BinaryOperator<ValueT> getSimpleOperator(String op) {
        if (op.equals("==")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                if (left.get() == null) {
                    return this.crawler.getDeterminedValue(right.get() == null);
                } else if (right.get() == null) {
                    return this.crawler.getDeterminedValue(false);
                }
                try {
                    boolean i = this.crawler.getAsBoolean(left).get();
                    boolean j = this.crawler.getAsBoolean(right).get();

                    return this.crawler.getDeterminedValue(i == j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i == j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                try {
                    double i = this.crawler.getAsDouble(left).get();
                    double j = this.crawler.getAsDouble(right).get();

                    return this.crawler.getDeterminedValue(i == j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                if (left.get() instanceof String s1 && right.get() instanceof String s2) {
                    return this.crawler.getDeterminedValue(s1.equals(s2));
                }

                throw new ClassCastException(
                        "incompatible types for addition: " + left.get().getClass() + " and " + right.get().getClass());
            };
        } else if (op.equals("+")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i + j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                try {
                    double i = this.crawler.getAsDouble(left).get();
                    double j = this.crawler.getAsDouble(right).get();

                    return this.crawler.getDeterminedValue(i + j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                if (left.get() instanceof String s1 && right.get() instanceof String s2) {
                    return this.crawler.getDeterminedValue(s1 + s2);
                }

                throw new ClassCastException(
                        "incompatible types for addition: " + left.get().getClass() + " and " + right.get().getClass());
            };
        } else if (op.equals("-")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i - j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                try {
                    double i = this.crawler.getAsDouble(left).get();
                    double j = this.crawler.getAsDouble(right).get();

                    return this.crawler.getDeterminedValue(i - j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException("incompatible types for subtraction: " + left.get().getClass() + " and "
                        + right.get().getClass());
            };
        } else if (op.equals("*")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i * j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                try {
                    double i = this.crawler.getAsDouble(left).get();
                    double j = this.crawler.getAsDouble(right).get();

                    return this.crawler.getDeterminedValue(i * j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException("incompatible types for multiplication: " + left.get().getClass() + " and "
                        + right.get().getClass());
            };
        } else if (op.equals("/")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i / j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                try {
                    double i = this.crawler.getAsDouble(left).get();
                    double j = this.crawler.getAsDouble(right).get();

                    return this.crawler.getDeterminedValue(i / j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException(
                        "incompatible types for division: " + left.get().getClass() + " and "
                                + right.get().getClass());
            };
        } else if (op.equals("&")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i & j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException("incompatible types for & operator: " + left.get().getClass() + " and "
                        + right.get().getClass());
            };
        } else if (op.equals("|")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i | j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException("incompatible types for | operator: " + left.get().getClass() + " and "
                        + right.get().getClass());
            };
        } else if (op.equals("<<")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i << j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                return null; // probably stream operator, ignore

                // throw new ClassCastException(
                // "incompatible types for << operator: " + left.get().getClass() + " and "
                // + right.get().getClass());
            };
        } else if (op.equals(">>")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    int i = this.crawler.getAsInteger(left).get();
                    int j = this.crawler.getAsInteger(right).get();

                    return this.crawler.getDeterminedValue(i >> j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException("incompatible types for >> operator: " + left.get().getClass() + " and "
                        + right.get().getClass());
            };
        } else if (op.equals("&&")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    boolean i = this.crawler.getAsBoolean(left).get();
                    boolean j = this.crawler.getAsBoolean(right).get();

                    return this.crawler.getDeterminedValue(i && j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException(
                        "incompatible types for && operator: " + left.get().getClass() + " and "
                                + right.get().getClass());
            };
        } else if (op.equals("||")) {
            return (left, right) -> {
                if (!left.isDetermined() || !right.isDetermined()) {
                    return this.crawler.getNonDeterminedValue();
                }
                try {
                    boolean i = this.crawler.getAsBoolean(left).get();
                    boolean j = this.crawler.getAsBoolean(right).get();

                    return this.crawler.getDeterminedValue(i || j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }
                try {
                    double i = this.crawler.getAsDouble(left).get();
                    double j = this.crawler.getAsDouble(right).get();

                    return this.crawler.getDeterminedValue(i / j);
                } catch (ClassCastException e) {
                    // ignore, carry on
                }

                throw new ClassCastException(
                        "incompatible types for || operator: " + left.get().getClass() + " and "
                                + right.get().getClass());
            };
        }
        // TODO: add more operators
        return null;
    }

    /**
     * Called when a function has been called (i.e. entered), right before the SmallStepResult is
     * created. Not called when entering special case functions (wait, notify, request_update).
     * 
     * Used to adjust the resulting state, i.e. for setting parameter values.
     *
     * @param expression the evaluated function call expression
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    public void functionCalled(FunctionCallExpression expression, TransitionResultT currentState,
            LocalStateT localState) {
        EvaluationContext<ValueT> callingContext = localState.getExecutionStack().get(localState.getExecutionStack().size() - 2);
        List<WrappedSCFunction> stackTrace = localState.getStackTrace();

        AccessedVariablesInformation writtenVariables = new AccessedVariablesInformation();
        localState.setStateInformation(VARIABLES_WRITTEN_KEY, writtenVariables);

        int i = 0;
        for (SCParameter param : expression.getFunction().getParameters()) {
            LocalVariable<SCVariable> var = new LocalVariable<SCVariable>(stackTrace, param.getVar());
            if (this.localVariableStorageCondition.test(var)) {
                ValueT value = callingContext.getExpressionValue(0, i);
                localState.setVariableValue(var, value);
            }
            writtenVariables.add(var);
            i++;
        }
    }


    /**
     * Called when a function has been returned from (i.e. left), right before the SmallStepResult is
     * created. Not called when leaving special case functions (wait, notify, request_update) or the top
     * level function.
     * 
     * Used to adjust the resulting state, i.e. for resetting parameter values.
     *
     * @param expression the function call expression which lead to the function beeing entered
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    protected void functionReturned(FunctionCallExpression expression, TransitionResultT currentState,
            LocalStateT localState) {
        List<WrappedSCFunction> stackTrace = new ArrayList<>(localState.getStackTrace());
        stackTrace.add(wrap(expression.getFunction()));

        for (SCParameter param : expression.getFunction().getParameters()) {
            LocalVariable<SCVariable> var = new LocalVariable<SCVariable>(stackTrace, param.getVar());
            if (this.localVariableStorageCondition.test(var)) {
                localState.deleteVariableValue(var);
            }
        }
    }

}
