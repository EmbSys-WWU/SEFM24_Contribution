package raid24contribution.dependencies;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import raid24contribution.statespace_exploration.AbstractedValue;

// might be moved later, just proof of concept for now
public class DefinitionReachabilityAnalysis {

    public static record Definition<NodeType extends CfgNode<NodeType, ?, VariableType, ?>, VariableType> (
            NodeType node, VariableType variable) {}

    public static record DefUseChain<NodeType extends CfgNode<NodeType, ?, VariableType, ?>, VariableType> (
            Definition<NodeType, VariableType> def, NodeType use) {

        @Override
        public String toString() {
            return this.def().variable() + " from " + def().node() + " to " + this.use();
        }
    }

    public static <NodeType extends CfgNode<NodeType, ?, VariableType, ?>, VariableType> Set<DefUseChain<NodeType, VariableType>> getDefUseChainsMemEff(
            ControlFlowGraph<NodeType, ?, VariableType, ?> graph) {
        Set<DefUseChain<NodeType, VariableType>> result = new LinkedHashSet<>();
        Deque<NodeType> defsWorklist = new ArrayDeque<>(List.of(graph.getEntryNode()));
        Set<NodeType> defsVisited = new LinkedHashSet<>(Set.of(graph.getEntryNode()));

        while (!defsWorklist.isEmpty()) {
            NodeType defNode = defsWorklist.poll();

            for (VariableType var : defNode.getPossiblyWrittenVariables()) {
                Definition<NodeType, VariableType> def = new Definition<NodeType, VariableType>(defNode, var);
                Deque<NodeType> usesWorklist = new ArrayDeque<>();
                Set<NodeType> usesVisited = new LinkedHashSet<>();

                for (CfgEdge<? extends NodeType, ?> edge : defNode.getOutgoingEdges()) {
                    usesWorklist.add(edge.getTarget());
                    usesVisited.add(edge.getTarget());
                }

                while (!usesWorklist.isEmpty()) {
                    NodeType useNode = usesWorklist.poll();
                    AbstractedValue<?, ?, Boolean> isRead = useNode.isVariableRead(var);
                    if (isRead.isUndetermined() || isRead.get()) {
                        result.add(new DefUseChain<NodeType, VariableType>(def, useNode));
                    }

                    AbstractedValue<?, ?, Boolean> isWritten = useNode.isVariableWritten(var);
                    if (isWritten.isDetermined() && isWritten.get()) {
                        continue;
                    }

                    for (CfgEdge<? extends NodeType, ?> edge : useNode.getOutgoingEdges()) {
                        if (usesVisited.add(edge.getTarget())) {
                            usesWorklist.add(edge.getTarget());
                        }
                    }
                }
            }

            for (CfgEdge<? extends NodeType, ?> edge : defNode.getOutgoingEdges()) {
                if (defsVisited.add(edge.getTarget())) {
                    defsWorklist.add(edge.getTarget());
                }
            }
        }

        return result;
    }

    public static <NodeType extends CfgNode<NodeType, ?, VariableType, ?>, VariableType> Set<DefUseChain<NodeType, VariableType>> getDefUseChains(
            ControlFlowGraph<NodeType, ?, VariableType, ?> graph) {
        Set<DefUseChain<NodeType, VariableType>> result = new LinkedHashSet<>();
        Deque<NodeType> worklist = new ArrayDeque<>(List.of(graph.getEntryNode()));
        Map<NodeType, Set<Definition<NodeType, VariableType>>> reachingDefinitions = new LinkedHashMap<>();
        reachingDefinitions.put(graph.getEntryNode(), new LinkedHashSet<>());

        while (!worklist.isEmpty()) {
            NodeType current = worklist.poll();

            Set<Definition<NodeType, VariableType>> reaching = reachingDefinitions.get(current);
            Set<Definition<NodeType, VariableType>> leaving = new LinkedHashSet<>();

            for (Definition<NodeType, VariableType> def : reaching) {
                AbstractedValue<?, ?, Boolean> read = current.isVariableRead(def.variable());
                if (read.isUndetermined() || read.get()) {
                    result.add(new DefUseChain<NodeType, VariableType>(def, current));
                }

                AbstractedValue<?, ?, Boolean> written = current.isVariableWritten(def.variable());
                if (written.isUndetermined() || !written.get()) {
                    leaving.add(def);
                }
            }

            for (VariableType var : current.getPossiblyWrittenVariables()) {
                leaving.add(new Definition<NodeType, VariableType>(current, var));
            }

            for (CfgEdge<? extends NodeType, ?> edge : current.getOutgoingEdges()) {
                NodeType successor = edge.getTarget();
                Set<Definition<NodeType, VariableType>> successorReaching = reachingDefinitions.get(successor);

                boolean changed = false;
                if (successorReaching == null) {
                    successorReaching = new LinkedHashSet<>();
                    reachingDefinitions.put(successor, successorReaching);
                    changed = true;
                }

                changed |= successorReaching.addAll(leaving);
                if (changed) {
                    worklist.add(successor);
                }
            }
        }

        return result;
    }

}
