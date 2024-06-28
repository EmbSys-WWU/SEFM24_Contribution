package de.tub.pes.syscir.dependencies;

import de.tub.pes.syscir.statespace_exploration.TransitionInformation;
import java.util.Collections;
import java.util.Set;

public class ControlFlowGraph<NodeType extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType>, EdgeType extends CfgEdge<NodeType, EdgeType>, VariableType, TransitionInformationType extends TransitionInformation<TransitionInformationType>> {

    private NodeType entryNode;
    private Set<NodeType> exitNodes;

    public ControlFlowGraph(NodeType entryNode, Set<NodeType> exitNodes) {
        this.entryNode = entryNode;
        this.exitNodes = Collections.unmodifiableSet(exitNodes);
    }

    public NodeType getEntryNode() {
        return this.entryNode;
    }

    public Set<NodeType> getExitNodes() {
        return this.exitNodes;
    }

}
