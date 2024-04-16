package raid24contribution.statespace_exploration.no_variables_implementation;

import java.util.Map;
import java.util.Set;
import raid24contribution.statespace_exploration.GlobalState;
import raid24contribution.statespace_exploration.TimedBlocker;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.util.WrappedSCClassInstance;


public class NoVariablesGlobalState extends GlobalState<NoVariablesGlobalState> {

    public NoVariablesGlobalState(Map<Event, TimedBlocker> eventStates, Set<WrappedSCClassInstance> requestedUpdates,
            boolean simulationStopped) {
        super(eventStates, requestedUpdates, simulationStopped);
    }

    public NoVariablesGlobalState(NoVariablesGlobalState copyOf) {
        super(copyOf);
    }

    @Override
    public NoVariablesGlobalState unlockedClone() {
        return new NoVariablesGlobalState(this);
    }

}
