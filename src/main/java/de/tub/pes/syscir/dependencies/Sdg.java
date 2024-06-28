package de.tub.pes.syscir.dependencies;

import de.tub.pes.syscir.dependencies.SdgNode.SdgNodeId;
import de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgEdge;
import de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgNode;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Sdg {

    private Map<SdgNodeId, SdgNode> nodes;

    public Sdg() {
        this.nodes = new LinkedHashMap<>();
    }

    public void integratePdg(PdgInformation pdg) {
        for (PdgNode node : pdg.getNodes().values()) {
            SdgNodeId id = new SdgNodeId(pdg, node.getId());
            SdgNode present = this.nodes.put(id, new SdgNode(id));
            assert present == null;
        }

        for (PdgEdge pdgEdge : pdg.getEdges()) {
            SdgNodeId sourceId = new SdgNodeId(pdg, pdgEdge.getSource().getId());
            SdgNodeId targetId = new SdgNodeId(pdg, pdgEdge.getTarget().getId());
            new SdgEdge(pdgEdge.getType(), this.nodes.get(sourceId), this.nodes.get(targetId), true);
        }
    }

    public Map<SdgNodeId, SdgNode> getNodes() {
        return this.nodes;
    }

    /**
     * Returns all edges of this SDG.
     * 
     * The returned collection is newly created and not backed by this SDG.
     * 
     * @return edges
     */
    public Set<SdgEdge> getEdges() {
        return this.nodes.values().stream().map(SdgNode::getOutgoing).flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SDG; Nodes: ");
        builder.append(this.nodes.values());
        builder.append(" Edges: ");
        builder.append(getEdges());
        return builder.toString();
    }

}
