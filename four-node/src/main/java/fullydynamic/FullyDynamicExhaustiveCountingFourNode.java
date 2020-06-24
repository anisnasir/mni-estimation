package fullydynamic;

import java.util.Set;

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

public class FullyDynamicExhaustiveCountingFourNode implements TopkGraphPatterns {
	NodeMap nodeMap;
	EdgeHandler utility;
	SupportCount supportCount;
	int numSubgraph;
	QuadripletGenerator subgraphGenerator;

	public FullyDynamicExhaustiveCountingFourNode() {
		this.nodeMap = new NodeMap();
		utility = new EdgeHandler();
		numSubgraph = 0;
		supportCount = new MapSupportCount();
	}

	@Override
	public boolean addEdge(StreamEdge edge) {
		// System.out.println("+" + edge);
		if (nodeMap.contains(edge)) {
			return false;
		}

		subgraphGenerator = new QuadripletGenerator();

		// System.out.println(nodeMap.map);
		LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
		LabeledNode dst = new LabeledNode(edge.getDestination(), edge.getDstLabel());
		THashSet<LabeledNode> srcOneHopNeighbor = nodeMap.getNeighbors(src);
		THashSet<Triplet> srcTwoHopNeighbors = nodeMap.getTwoHopNeighbors(src);
		THashSet<LabeledNode> dstOneHopNeighbor = nodeMap.getNeighbors(dst);
		THashSet<Triplet> dstTwoHopNeighbors = nodeMap.getTwoHopNeighbors(dst);

		// long startTime = System.nanoTime();
		Set<Quadriplet> subgraphs = subgraphGenerator.getAllSubgraphs(nodeMap, edge, src, dst, srcOneHopNeighbor,
				dstOneHopNeighbor, srcTwoHopNeighbors, dstTwoHopNeighbors);

		// System.out.println("step 1 " + (System.nanoTime()-startTime));
		for (Quadriplet subgraph : subgraphs) {
			if (subgraph.getType() == SubgraphType.LINE || subgraph.getType() == SubgraphType.STAR) {
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

	@Override
	public boolean removeEdge(StreamEdge edge) {
		if (!nodeMap.contains(edge))
			return false;
		// System.out.println("-" + edge);
		utility.handleEdgeDeletion(edge, nodeMap);
		subgraphGenerator = new QuadripletGenerator();

		// System.out.println(nodeMap.map);
		LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
		LabeledNode dst = new LabeledNode(edge.getDestination(), edge.getDstLabel());
		THashSet<LabeledNode> srcOneHopNeighbor = nodeMap.getNeighbors(src);
		THashSet<Triplet> srcTwoHopNeighbors = nodeMap.getTwoHopNeighbors(src);
		THashSet<LabeledNode> dstOneHopNeighbor = nodeMap.getNeighbors(dst);
		THashSet<Triplet> dstTwoHopNeighbors = nodeMap.getTwoHopNeighbors(dst);

		// long startTime = System.nanoTime();
		Set<Quadriplet> subgraphs = subgraphGenerator.getAllSubgraphs(nodeMap, edge, src, dst, srcOneHopNeighbor,
				dstOneHopNeighbor, srcTwoHopNeighbors, dstTwoHopNeighbors);

		// System.out.println("step 1 " + (System.nanoTime()-startTime));
		for (Quadriplet subgraph : subgraphs) {
			removeSubgraph(subgraph);
			Quadriplet quadripletMinusEdge = subgraph.getQuadripletMinusEdge(edge);
			if (quadripletMinusEdge.isQuadriplet()) {
				addSubgraph(quadripletMinusEdge);
			}
		}

		// System.out.println(counter);
		return true;
	}

	void removeSubgraph(Quadriplet t) {
		/*
		 * if(counter.containsKey(t)) { int count = counter.get(t); if(count > 1)
		 * counter.put(t, count-1); else counter.remove(t); numSubgraph--;
		 * removeFrequentPattern(t); } else { System.out.println("remove error " + t);
		 * System.out.println(counter); System.exit(1); }
		 */
		// removeFrequentPattern(t);
		numSubgraph--;
		removeFrequentPattern(t);

	}

	void addSubgraph(Quadriplet t) {
		/*
		 * if(counter.containsKey(t)) { int count = counter.get(t); counter.put(t,
		 * count+1); }else { counter.put(t, 1); }
		 */
		addFrequentPattern(t);
		numSubgraph++;
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
	public int getCurrentReservoirSize() {
		return 0;
	}

	@Override
	public THashMap<Pattern, Long> correctEstimates() {
		return this.supportCount.getPatternCount();
		
	}

}
