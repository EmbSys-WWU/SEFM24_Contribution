package de.tub.pes.syscir.dependencies;

import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.TransitionInformation;
import java.util.Set;

public interface CfgNode<NodeType extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType>, EdgeType extends CfgEdge<NodeType, EdgeType>, VariableType, TransitionInformationType extends TransitionInformation<TransitionInformationType>> {

    AbstractedValue<?, ?, Boolean> isVariableRead(VariableType var);

    AbstractedValue<?, ?, Boolean> isVariableWritten(VariableType var);

    Set<VariableType> getPossiblyWrittenVariables();

    Set<EdgeType> getIncomingEdges();

    Set<EdgeType> getOutgoingEdges();

    TransitionInformationType getTransitionInformation();

}
