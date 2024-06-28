package de.tub.pes.syscir.statespace_exploration.no_variables_implementation;

import de.tub.pes.syscir.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.statespace_exploration.GlobalState;
import de.tub.pes.syscir.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.util.WrappedSCClassInstance;
import java.util.Map;
import java.util.Set;


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
