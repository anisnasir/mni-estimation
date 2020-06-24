package incremental;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import graphpattern.FourNodeGraphPattern;
import input.StreamEdge;
import struct.LabeledNode;
import struct.NodeMap;
import struct.Quadriplet;
import struct.Triplet;
import support.MapSupportCount;
import support.SupportCount;
import topkgraphpattern.Pattern;
import topkgraphpattern.SubgraphType;
import topkgraphpattern.TopkGraphPatterns;
import utility.EdgeHandler;
import utility.QuadripletGenerator;

import java.util.Set;

public class IncrementalExhaustiveCountingFourNode implements TopkGraphPatterns {
	NodeMap nodeMap;
	EdgeHandler utility;
	SupportCount supportCount;
	long numSubgraph;
	QuadripletGenerator subgraphGenerator;

	public IncrementalExhaustiveCountingFourNode() {
		utility = new EdgeHandler();
		numSubgraph = 0;
		supportCount = new MapSupportCount();
		this.nodeMap = new NodeMap();
		subgraphGenerator = new QuadripletGenerator();
	}

	@Override
	public boolean addEdge(StreamEdge edge) {
		// System.out.println("+" + edge);
		if (nodeMap.contains(edge)) {
			return false;
		}

		// System.out.println(nodeMap.map);
		LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
		LabeledNode dst = new LabeledNode(edge.getDestination(), edge.getDstLabel());
		THashSet<LabeledNode> srcOneHopNeighbor = nodeMap.getNeighbors(src);
		THashSet<Triplet> srcTwoHopNeighbors = nodeMap.getTwoHopNeighbors(src);
		THashSet<LabeledNode> dstOneHopNeighbor = nodeMap.getNeighbors(dst);
		THashSet<Triplet> dstTwoHopNeighbors = nodeMap.getTwoHopNeighbors(dst);
		
		//long startTime = System.nanoTime();
		Set<Quadriplet> subgraphs = subgraphGenerator.getAllSubgraphs(nodeMap, edge, src, dst, srcOneHopNeighbor, dstOneHopNeighbor, srcTwoHopNeighbors, dstTwoHopNeighbors);

		//System.out.println("step 1 " + (System.nanoTime()-startTime));
		for(Quadriplet subgraph: subgraphs) {
			if (subgraph.getType().equals(SubgraphType.LINE) || subgraph.getType().equals(SubgraphType.STAR)) {
				addSubgraph(subgraph);
			} else if (subgraph.getType().equals(SubgraphType.TAILED_TRIANGLE)) {
				Quadriplet quadripletMinusEdge = subgraph.getQuadripletMinusEdge(edge);
				if(quadripletMinusEdge.isQuadriplet()) {
					removeSubgraph(quadripletMinusEdge);
				}
				addSubgraph(subgraph);
			} else {
				addSubgraph(subgraph);
				removeSubgraph(subgraph.getQuadripletMinusEdge(edge));
			}
		}

		utility.handleEdgeAddition(edge, nodeMap);
		// System.out.println(counter);
		return false;
	}

	void removeSubgraph(Quadriplet t) {
		if (t.isQuadriplet()) {
			numSubgraph--;
			removeFrequentPattern(t);
		}
	}

	void addSubgraph(Quadriplet t) {
		if (t.isQuadriplet()) {
			addFrequentPattern(t);
			numSubgraph++;
		}
	}

	void addFrequentPattern(Quadriplet t) {
		FourNodeGraphPattern p = new FourNodeGraphPattern(t);
		supportCount.add(p);
	}

	void removeFrequentPattern(Quadriplet t) {
		FourNodeGraphPattern p = new FourNodeGraphPattern(t);
		supportCount.remove(p);
	}

	@Override
	public THashMap<Pattern, Long> getFrequentPatterns() {
		return this.supportCount.getPatternCount();
	}

	@Override
	public long getNumberofSubgraphs() {
		return this.numSubgraph;
	}

	@Override
	public boolean removeEdge(StreamEdge edge) {
		//Unimplemented for 
		return false;
	}

	@Override
	public int getCurrentReservoirSize() {
		return 0;
	}

	@Override
	public THashMap<Pattern, Long> correctEstimates() {
		return this.supportCount.getPatternCount();
		
	}

}
