package raid24contribution.dependencies;

import java.util.Set;
import raid24contribution.statespace_exploration.AbstractedValue;
import raid24contribution.statespace_exploration.TransitionInformation;

public interface CfgNode<NodeType extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType>, EdgeType extends CfgEdge<NodeType, EdgeType>, VariableType, TransitionInformationType extends TransitionInformation<TransitionInformationType>> {

    AbstractedValue<?, ?, Boolean> isVariableRead(VariableType var);

    AbstractedValue<?, ?, Boolean> isVariableWritten(VariableType var);

    Set<VariableType> getPossiblyWrittenVariables();

    Set<EdgeType> getIncomingEdges();

    Set<EdgeType> getOutgoingEdges();

    TransitionInformationType getTransitionInformation();

}
