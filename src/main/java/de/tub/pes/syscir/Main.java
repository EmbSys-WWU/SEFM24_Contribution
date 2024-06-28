package de.tub.pes.syscir;

import static de.tub.pes.syscir.util.WrapperUtil.wrap;

import de.tub.pes.syscir.dependencies.Sdg;
import de.tub.pes.syscir.dependencies.SdgEdge;
import de.tub.pes.syscir.dependencies.SdgFromInterleavingCfg;
import de.tub.pes.syscir.dependencies.SdgNode;
import de.tub.pes.syscir.engine.Engine;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCPortEvent;
import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.statespace_exploration.ExplorationRecord;
import de.tub.pes.syscir.statespace_exploration.GlobalState;
import de.tub.pes.syscir.statespace_exploration.ProcessState;
import de.tub.pes.syscir.statespace_exploration.Scheduler;
import de.tub.pes.syscir.statespace_exploration.Scheduler.SimulationStopMode;
import de.tub.pes.syscir.statespace_exploration.SequentialStateSpaceExploration;
import de.tub.pes.syscir.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.statespace_exploration.no_variables_implementation.NoVariablesNoInformationProcess;
import de.tub.pes.syscir.statespace_exploration.no_variables_implementation.NoVariablesNoInformationScheduler;
import de.tub.pes.syscir.statespace_exploration.some_variables_implementation.SomeVariablesGlobalState;
import de.tub.pes.syscir.statespace_exploration.some_variables_implementation.SomeVariablesProcess;
import de.tub.pes.syscir.statespace_exploration.some_variables_implementation.SomeVariablesProcessState;
import de.tub.pes.syscir.statespace_exploration.some_variables_implementation.SomeVariablesScheduler;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.CfgLikeRecord;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.TransitionGraphRecord;
import de.tub.pes.syscir.statespace_exploration.transition_informations.NoInformation;
import de.tub.pes.syscir.statespace_exploration.transition_informations.VariablesReadWrittenInformation;
import de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.util.TriFunction;
import de.tub.pes.syscir.util.WrappedSCClassInstance;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Main {

    public static final Predicate<Object> ALWAYS_TRUE = x -> true;
    public static final Predicate<Object> ALWAYS_FALSE = x -> false;

    public static TriFunction<SCSystem, SCProcess, SCClassInstance, NoVariablesNoInformationProcess> processConstructor(
            NoVariablesNoInformationScheduler scheduler) {
        return (s, p, i) -> new NoVariablesNoInformationProcess(s, p, i, scheduler);
    }

    public static TriFunction<SCSystem, SCProcess, SCClassInstance, SomeVariablesProcess<BinaryAbstractedValue<?>, NoInformation>> noInformationProcessConstructor(
            SomeVariablesScheduler<BinaryAbstractedValue<?>, NoInformation> scheduler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
            Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return (s, p, i) -> SomeVariablesProcess.withBinaryAbstractionAndNoInformation(s, p, i, scheduler,
                globalVariableStorageCondition, localVariableStorageCondition);
    }

    public static TriFunction<SCSystem, SCProcess, SCClassInstance, SomeVariablesProcess<BinaryAbstractedValue<?>, VariablesReadWrittenInformation<BinaryAbstractedValue<Boolean>>>> variablesReadWrittenInformationProcessConstructor(
            SomeVariablesScheduler<BinaryAbstractedValue<?>, VariablesReadWrittenInformation<BinaryAbstractedValue<Boolean>>> scheduler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
            Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return (s, p, i) -> SomeVariablesProcess.withBinaryAbstractionAndVariablesReadWrittenInformation(s, p, i,
                scheduler, globalVariableStorageCondition, localVariableStorageCondition);
    }

    public static TriFunction<SCSystem, SCProcess, SCClassInstance, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> pdgInformationProcessConstructor(
            SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> scheduler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
            Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return (s, p, i) -> SomeVariablesProcess.withBinaryAbstractionAndPdgInformation(s, p, i, scheduler,
                globalVariableStorageCondition, localVariableStorageCondition);
    }

    public static <G extends GlobalState<G>, P extends ProcessState<P, ?>, T extends TransitionInformation<T>, ProcessT extends AnalyzedProcess<ProcessT, G, P, T>> StateSpaceExploration<G, P, T, ProcessT> constructExplorer(
            Scheduler<G, P, T, ProcessT> scheduler, ExplorationRecord<G, P, T> record,
            Set<ConsideredState<G, P, ProcessT>> initialStates) {
        return new SequentialStateSpaceExploration<>(scheduler, record, initialStates);
    }

    public static <G extends GlobalState<G>, P extends ProcessState<P, ?>, T extends ComposableTransitionInformation<T>> TransitionGraphRecord<G, P, T> constructRecord() {
        return new TransitionGraphRecord<>(false);
    }

    public static <V extends AbstractedValue<V, ?, ?>> TriFunction<Map<Event, TimedBlocker>, Set<WrappedSCClassInstance>, Boolean, SomeVariablesGlobalState<V>> globalStateConstructor(
            SCSystem scSystem, Function<Object, ? extends V> determinedValueConstructor) {
        return (eventStates, requestedUpdates, simulationStopped) -> {
            SomeVariablesGlobalState<V> result = new SomeVariablesGlobalState<>(eventStates, requestedUpdates,
                    simulationStopped, new LinkedHashMap<>());

            for (SCConnectionInterface con : scSystem.getPortSocketInstances()) {
                SCPortInstance portInstance = (SCPortInstance) con;

                result.setVariableValue(
                        new GlobalVariable<>(wrap(portInstance.getOwner()), portInstance.getPortSocket()),
                        determinedValueConstructor.apply(wrap(portInstance.getChannels().get(0))));

                if (portInstance.getPortSocket().getType().equals("sc_signal")) {
                    SCEvent variable = new SCEvent("change", false, false, List.of());
                    result.setVariableValue(new GlobalVariable<>(portInstance, variable),
                            determinedValueConstructor.apply(new Event(portInstance.getName() + ".change")));
                }
            }

            for (SCClassInstance classInstance : scSystem.getInstances()) {
                for (SCEvent scEvent : classInstance.getSCClass().getEvents()) {
                    if (scEvent instanceof SCPortEvent) {
                        continue;
                    }
                    result.setVariableValue(new GlobalVariable<>(wrap(classInstance), scEvent),
                            determinedValueConstructor.apply(new Event(scEvent)));
                }
                for (SCVariable scVariable : classInstance.getSCClass().getMembers()) {
                    if (scVariable.getType().startsWith("sc_signal")) {
                        SCClassInstance value =
                                scSystem.getInstances().stream().filter(i -> i.getType().equals(scVariable.getType())
                                        && i.getName().equals(scVariable.getName())).findAny().get();
                        result.setVariableValue(new GlobalVariable<>(wrap(classInstance), scVariable),
                                determinedValueConstructor.apply(wrap(value)));
                    }

                    if (scVariable.getType().startsWith("sc_fifo")) {
                        continue;
                    }

                }
            }

            return result;
        };
    }

    public static <G extends SomeVariablesGlobalState<V>, V extends AbstractedValue<V, ?, ?>, ProcessT extends SomeVariablesProcess<V, ?>> Set<Event> initialSensitivitiesGetter(
            ProcessT process, G globalState) {
        return process.getSensitivities(globalState);
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.runningExample();
        main.timingAgnosticExample();
        main.timingLeakExample();
        main.changeDependentExample();
        main.gpioBasedExample();
    }

    public void runningExample() throws IOException {
        String path = "examples/running_example/";
        SCSystem scSystem = Engine.buildModelFromFile(path + "sc2ast.ast.xml");


        Predicate<GlobalVariable<?, ?>> eventsAndPortsOnlyGlobal =
                x -> x.getSCVariable() instanceof SCEvent || x.getSCVariable() instanceof SCPort;

        SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> scheduler = SomeVariablesScheduler
                .withBinaryAbstractionAndPdgInformation(scSystem, SimulationStopMode.SC_STOP_FINISH_IMMEDIATE,
                        ALWAYS_TRUE, eventsAndPortsOnlyGlobal, ALWAYS_FALSE);

        ConsideredState<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> initialState =
                ConsideredState.getInitialState(scSystem, globalStateConstructor(scSystem, BinaryAbstractedValue::of),
                        pdgInformationProcessConstructor(scheduler, eventsAndPortsOnlyGlobal, ALWAYS_FALSE),
                        SomeVariablesProcessState::new, Main::initialSensitivitiesGetter, BinaryAbstractedValue::of);

        executeExample("RunningExample", path, scheduler, initialState, "(STATEMENT [paperProdcons pc;, pump:[0, 3]])",
                Map.of("Untrusted input", "(IN GVar[paperProdcons pc;.int UNTRUSTED_IN;])", "Trusted input",
                        "(IN GVar[paperProdcons pc;.int TRUSTED_IN;])"));
    }

    public void timingAgnosticExample() throws IOException {
        String path = "examples/timing_agnostic/";
        SCSystem scSystem = Engine.buildModelFromFile(path + "sc2ast.ast.xml");


        Predicate<GlobalVariable<?, ?>> eventsAndPortsOnlyGlobal =
                x -> x.getSCVariable() instanceof SCEvent || x.getSCVariable() instanceof SCPort;

        SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> scheduler = SomeVariablesScheduler
                .withBinaryAbstractionAndPdgInformation(scSystem, SimulationStopMode.SC_STOP_FINISH_IMMEDIATE,
                        ALWAYS_TRUE, eventsAndPortsOnlyGlobal, ALWAYS_FALSE);

        ConsideredState<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> initialState =
                ConsideredState.getInitialState(scSystem, globalStateConstructor(scSystem, BinaryAbstractedValue::of),
                        pdgInformationProcessConstructor(scheduler, eventsAndPortsOnlyGlobal, ALWAYS_FALSE),
                        SomeVariablesProcessState::new, Main::initialSensitivitiesGetter, BinaryAbstractedValue::of);

        executeExample("TimingAgnostic", path, scheduler, initialState,
                "(STATEMENT [paperTimeAgnostic ta;, pump:[0, 3]])",
                Map.of("Untrusted input", "(IN GVar[paperTimeAgnostic ta;.int UNTRUSTED_IN;])", "Trusted input",
                        "(IN GVar[paperTimeAgnostic ta;.int TRUSTED_IN;])"));
    }

    public void timingLeakExample() throws IOException {
        String path = "examples/timing_leak/";
        SCSystem scSystem = Engine.buildModelFromFile(path + "sc2ast.ast.xml");


        Predicate<GlobalVariable<?, ?>> eventsAndPortsOnlyGlobal =
                x -> x.getSCVariable() instanceof SCEvent || x.getSCVariable() instanceof SCPort;

        SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> scheduler = SomeVariablesScheduler
                .withBinaryAbstractionAndPdgInformation(scSystem, SimulationStopMode.SC_STOP_FINISH_IMMEDIATE,
                        ALWAYS_TRUE, eventsAndPortsOnlyGlobal, ALWAYS_FALSE);

        ConsideredState<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> initialState =
                ConsideredState.getInitialState(scSystem, globalStateConstructor(scSystem, BinaryAbstractedValue::of),
                        pdgInformationProcessConstructor(scheduler, eventsAndPortsOnlyGlobal, ALWAYS_FALSE),
                        SomeVariablesProcessState::new, Main::initialSensitivitiesGetter, BinaryAbstractedValue::of);

        executeExample("TimingLeak", path, scheduler, initialState, "(STATEMENT [paperTimingLeak tl;, pump:[0, 3]])",
                Map.of("Untrusted input", "(IN GVar[paperTimingLeak tl;.int UNTRUSTED_IN;])", "Trusted input",
                        "(IN GVar[paperTimingLeak tl;.int TRUSTED_IN;])"));
    }

    public void changeDependentExample() throws IOException {
        String path = "examples/change_dependent/";
        SCSystem scSystem = Engine.buildModelFromFile(path + "sc2ast.ast.xml");


        Predicate<GlobalVariable<?, ?>> eventsAndPortsOnlyGlobal =
                x -> x.getSCVariable() instanceof SCEvent || x.getSCVariable() instanceof SCPort;

        SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> scheduler = SomeVariablesScheduler
                .withBinaryAbstractionAndPdgInformation(scSystem, SimulationStopMode.SC_STOP_FINISH_IMMEDIATE,
                        ALWAYS_TRUE, eventsAndPortsOnlyGlobal, ALWAYS_FALSE);

        ConsideredState<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> initialState =
                ConsideredState.getInitialState(scSystem, globalStateConstructor(scSystem, BinaryAbstractedValue::of),
                        pdgInformationProcessConstructor(scheduler, eventsAndPortsOnlyGlobal, ALWAYS_FALSE),
                        SomeVariablesProcessState::new, Main::initialSensitivitiesGetter, BinaryAbstractedValue::of);

        executeExample("ChangeDependent", path, scheduler, initialState,
                "(STATEMENT [paperChangeDependent cd;, pump:[0, 4]])",
                Map.of("Untrusted input", "(IN GVar[paperChangeDependent cd;.int UNTRUSTED_IN;])", "Trusted input",
                        "(IN GVar[paperChangeDependent cd;.int TRUSTED_IN;])"));
    }

    public void gpioBasedExample() throws IOException {
        String path = "examples/gpio_based/";
        SCSystem scSystem = Engine.buildModelFromFile(path + "sc2ast.ast.xml");

        Event convertBitsEvent = new Event("convert_bits.change");
        Event convertWordEvent = new Event("convert_word.change");
        Event bitsChangeEvent = new Event("bits_change");
        Event wordChangeEvent = new Event("word_change");
        Set<Event> consideredEvents = Set.of(convertBitsEvent, convertWordEvent, bitsChangeEvent, wordChangeEvent);
        Predicate<Event> eventsPredicate = consideredEvents::contains;

        Predicate<GlobalVariable<?, ?>> globalVarsPredicate = x -> {
            if (x.getSCVariable() instanceof SCEvent || x.getSCVariable() instanceof SCPort) {
                return true;
            }
            if (!(x.getSCVariable() instanceof SCVariable v)) {
                return false;
            }
            if (v.isConst() || v.getType().startsWith("sc_signal") || v.getType().startsWith("sc_fifo")) {
                return true;
            }
            if (x.getQualifier() instanceof WrappedSCClassInstance ci && ci.getName().equals("convert_bits")) {
                return v.getName().equals("val") || v.getName().equals("_val");
            }
            if (x.getQualifier() instanceof WrappedSCClassInstance ci && ci.getName().equals("convert_word")) {
                return v.getName().equals("val") || v.getName().equals("_val");
            }
            return false;
        };
        Predicate<LocalVariable<?>> localVarsPredicate = x -> {
            return x.getSCVariable() instanceof SCVariable v && (v.isConst() || v.getName().equals("newval"));
        };

        SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> scheduler = SomeVariablesScheduler
                .withBinaryAbstractionAndPdgInformation(scSystem, SimulationStopMode.SC_STOP_FINISH_IMMEDIATE,
                        eventsPredicate, globalVarsPredicate, localVarsPredicate);

        ConsideredState<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> initialState =
                ConsideredState.getInitialState(scSystem, globalStateConstructor(scSystem, BinaryAbstractedValue::of),
                        pdgInformationProcessConstructor(scheduler, globalVarsPredicate, localVarsPredicate),
                        SomeVariablesProcessState::new, Main::initialSensitivitiesGetter, BinaryAbstractedValue::of);

        initialState = initialState.unlockedClone();
        initialState.getProcessStates().values().stream().forEach(ps -> {
            if (!(ps.getWaitingFor() instanceof EventBlocker eb)) {
                return;
            }
            String name = eb.getEvents().iterator().next().toString();
            if (name.equals(wordChangeEvent.getName())) {
                ps.setWaitingFor(eb.replaceEvents(Set.of(wordChangeEvent)));
            } else if (name.equals(bitsChangeEvent.getName())) {
                ps.setWaitingFor(eb.replaceEvents(Set.of(bitsChangeEvent)));
            }
        });
        for (SCClassInstance i : scSystem.getInstances()) {
            for (SCVariable v : i.getSCClass().getMembers()) {
                if (!globalVarsPredicate.test(new GlobalVariable<>(wrap(i), v))) {
                    continue;
                }
                if (v.getType().startsWith("sc_uint")) {
                    initialState.getGlobalState().setVariableValue(new GlobalVariable<>(wrap(i), v),
                            BinaryAbstractedValue.of(0));
                }
                if (v.getName().equals("val") || v.getName().equals("_val")) {
                    initialState.getGlobalState().setVariableValue(new GlobalVariable<>(wrap(i), v),
                            BinaryAbstractedValue.of(i.getType().contains("bool") ? false : 0));
                }
            }
            for (SCEvent e : i.getSCClass().getEvents()) {
                if (!globalVarsPredicate.test(new GlobalVariable<>(wrap(i), e))) {
                    continue;
                }
                if (e.getName().equals("change") && i.getName().equals("convert_bits")) {
                    initialState.getGlobalState().setVariableValue(new GlobalVariable<>(wrap(i), e),
                            BinaryAbstractedValue.of(convertBitsEvent));
                } else if (e.getName().equals("change") && i.getName().equals("convert_word")) {
                    initialState.getGlobalState().setVariableValue(new GlobalVariable<>(wrap(i), e),
                            BinaryAbstractedValue.of(convertWordEvent));
                } else if (e.getName().equals("bits_change")) {
                    initialState.getGlobalState().setVariableValue(new GlobalVariable<>(wrap(i), e),
                            BinaryAbstractedValue.of(bitsChangeEvent));
                } else if (e.getName().endsWith("word_change")) {
                    initialState.getGlobalState().setVariableValue(new GlobalVariable<>(wrap(i), e),
                            BinaryAbstractedValue.of(wordChangeEvent));
                }
            }
        }
        initialState.lock();

        executeExample("GpioExample", path, scheduler, initialState, "(STATEMENT [gpioTest gpioTest;, pump:[1, 1]])",
                Map.of("Sensor", "(IN GVar[gpioTest gpioTest;.sc_uint<8> SENSOR_VALUE;])", "WiFi",
                        "(IN GVar[gpioTest gpioTest;.sc_uint<8> WIFI_VALUE;])"));
    }

    public void executeExample(String name, String path,
            SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> scheduler,
            ConsideredState<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> initialState,
            String slicingCriterion, Map<String, String> nodesOfInterest) throws IOException {
        CfgLikeRecord<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, PdgInformation> record =
                new CfgLikeRecord<>(false, PdgInformation::getReadVariables, PdgInformation::getWrittenVariables,
                        initialState);
        StateSpaceExploration<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, PdgInformation, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> explorer =
                constructExplorer(scheduler, record, Set.of(initialState));

        long explorationStartTime = System.currentTimeMillis();
        explorer.run();
        long explorationEndTime = System.currentTimeMillis();
        long explorationTime = explorationEndTime - explorationStartTime;

        long integrationStartTime = System.currentTimeMillis();
        Sdg sdg = SdgFromInterleavingCfg.create(record);
        long integrationEndTime = System.currentTimeMillis();
        long integrationTime = integrationEndTime - integrationStartTime;

        Set<SdgEdge> edges = sdg.getEdges();

        long slicingTime = 0;
        Set<SdgNode> criteria = new LinkedHashSet<>();
        Set<SdgNode> slice = new LinkedHashSet<>();
        for (SdgNode node : sdg.getNodes().values().stream()
                .filter(n -> n.getId().id().toString().equals(slicingCriterion)).toList()) {
            criteria.add(node);
            long start = System.currentTimeMillis();
            slice.addAll(node.backwardsSlice());
            long end = System.currentTimeMillis();
            slicingTime += (end - start);
        }

        Map<String, Boolean> nodesOfInteresetContained = new LinkedHashMap<>();
        nodesOfInterest.entrySet().stream().forEach(e -> {
            nodesOfInteresetContained.put(e.getKey(),
                    slice.stream().anyMatch(n -> n.getId().id().toString().equals(e.getValue())));
        });

        Pattern compactionPattern = Pattern.compile(
                Pattern.quote("de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgInformation@")
                        + "[0-9a-f]+ ");
        Pattern displayPattern = Pattern.compile(
                Pattern.quote("de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgInformation"));
        Function<Object, String> displayMapper = ((Function<Object, String>) String::valueOf)
                .andThen(displayPattern::matcher).andThen(m -> m.replaceAll(""));

        Set<String> compactedSlice = new LinkedHashSet<>();
        for (SdgNode node : slice) {
            String s = node.toString();
            s = compactionPattern.matcher(s).replaceAll("");
            compactedSlice.add(s);
        }

        Set<String> compactedNodes = new LinkedHashSet<>();
        for (SdgNode node : sdg.getNodes().values()) {
            String s = node.toString();
            s = compactionPattern.matcher(s).replaceAll("");
            compactedNodes.add(s);
        }

        Set<String> compactedEdges = new LinkedHashSet<>();
        for (SdgEdge edge : edges) {
            String s = edge.toString();
            s = compactionPattern.matcher(s).replaceAll("");
            compactedEdges.add(s);
        }

        File outFile = new File(new File(path), "Report.txt");
        outFile.createNewFile();
        FileWriter fileWriter = new FileWriter(outFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        PrintStream output = new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                System.out.write(b);
                printWriter.write(b);
            }

        });

        DecimalFormat percentageFormat = new DecimalFormat("###,##0.0 %");

        output.println("=== Report on " + name + " ===");
        output.println();

        output.println("TIG #nodes: " + explorer.getNumExploredStates());
        output.println("SysCDG #nodes: " + sdg.getNodes().size());
        output.println("SysCDG #nodes (compacted): " + compactedNodes.size());
        output.println("SysCDG %nodes (compacted): "
                + percentageFormat.format(compactedNodes.size() / (double) sdg.getNodes().size()));
        output.println("SysCDG #edges: " + edges.size());
        output.println("SysCDG #edges (compacted): " + compactedEdges.size());
        output.println("SysCDG %edges (compacted): "
                + percentageFormat.format(compactedEdges.size() / (double) sdg.getEdges().size()));
        output.println("Slice #nodes: " + slice.size());
        output.println("Slice %nodes: " + percentageFormat.format(slice.size() / (double) sdg.getNodes().size()));
        output.println("Slice #nodes (compacted): " + compactedSlice.size());
        output.println("Slice %nodes (compacted): "
                + percentageFormat.format(compactedSlice.size() / (double) compactedNodes.size()));
        output.println();

        output.println("Symbolic execution time: " + explorationTime + " ms");
        output.println("Integrated analysis time: " + integrationTime + " ms");
        output.println("Slicing time: " + slicingTime + " ms");
        output.println();

        printWriter.println("Slicing criteria:");
        criteria.stream().map(displayMapper).forEach(printWriter::println);
        printWriter.println();

        for (Entry<String, Boolean> nodeOfInterest : nodesOfInteresetContained.entrySet()) {
            output.println(nodeOfInterest.getKey() + " contained: " + nodeOfInterest.getValue());
        }
        output.println();

        printWriter.println("Compacted slice:");
        compactedSlice.stream().forEach(printWriter::println);
        printWriter.println();

        printWriter.println("Compacted SysCDG nodes:");
        compactedNodes.stream().forEach(printWriter::println);
        printWriter.println();

        printWriter.println("Compacted SysCDG edges:");
        compactedEdges.stream().forEach(printWriter::println);
        printWriter.println();

        printWriter.println("Slice:");
        slice.stream().map(displayMapper).forEach(printWriter::println);
        printWriter.println();

        printWriter.println("SysCDG nodes:");
        sdg.getNodes().values().stream().map(displayMapper).forEach(printWriter::println);
        printWriter.println();

        printWriter.println("SysCDG edges:");
        edges.stream().map(displayMapper).forEach(printWriter::println);
        printWriter.println();

        printWriter.close();
    }

}
