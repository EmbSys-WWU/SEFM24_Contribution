package raid24contribution.dependencies;

import raid24contribution.statespace_exploration.TransitionInformation;

public interface CfgCallNode<NodeType extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType>, EdgeType extends CfgEdge<NodeType, EdgeType>, VariableType, TransitionInformationType extends TransitionInformation<TransitionInformationType>, CallTargetType extends Callable>
        extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType> {

    CallTargetType getCalled();

}
