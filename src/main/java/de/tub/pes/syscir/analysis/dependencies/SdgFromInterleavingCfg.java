package de.tub.pes.syscir.analysis.dependencies;

import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.dependencies.SdgNode.SdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;

public class SdgFromInterleavingCfg {
    
    public static Sdg create(CfgLikeRecord<?, ?, PdgInformation> record) {
        Sdg sdg = new Sdg();
        
        for (var node : record.getNodes()) {
            if (node.getTransitionInformation() == null) {
                continue;
            }
            sdg.integratePdg(node.getTransitionInformation());
        }
        
        var cfg = record.toCfg();
        var defUseChains = DefinitionReachabilityAnalysis.getDefUseChainsMemEff(cfg);
        
        
        for (var chain : defUseChains) {
            CfgLikeRecord<?, ?, PdgInformation>.Node defNode = chain.def().node();
            CfgLikeRecord<?, ?, PdgInformation>.Node useNode = chain.use();
            
            SdgNodeId defNodeId =
                    new SdgNodeId(defNode.getTransitionInformation(), NodeType.OUT, chain.def().variable());
            SdgNodeId useNodeId =
                    new SdgNodeId(useNode.getTransitionInformation(), NodeType.IN, chain.def().variable());
            SdgNode defPdgNode = sdg.getNodes().get(defNodeId);
            SdgNode usePdgNode = sdg.getNodes().get(useNodeId);
            new SdgEdge(EdgeType.DATA, defPdgNode, usePdgNode, true);
        }
        
        return sdg;
    }
    
}
