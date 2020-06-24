package incremental;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import graphpattern.ThreeNodeGraphPattern;
import input.StreamEdge;
import struct.LabeledNode;
import struct.NodeMap;
import struct.Triplet;
import support.MapSupportCount;
import support.SupportCount;
import topkgraphpattern.Pattern;
import topkgraphpattern.Subgraph;
import topkgraphpattern.TopkGraphPatterns;
import utility.EdgeHandler;
import utility.SetFunctions;

import java.util.Set;

public class IncrementalExhaustiveCountingThreeNode implements TopkGraphPatterns {
	NodeMap nodeMap;
	EdgeHandler utility;
	THashMap<Subgraph, Long> counter;
	SupportCount supportCount;
	long numSubgraph;

	public IncrementalExhaustiveCountingThreeNode() {
		utility = new EdgeHandler();
		counter = new THashMap<Subgraph, Long>();
		numSubgraph = 0;
		this.supportCount = new MapSupportCount();
		this.nodeMap = new NodeMap();
	}

	@Override
	public boolean addEdge(StreamEdge edge) {
		// System.out.println("+" + edge);
		if (nodeMap.contains(edge))
			return false;

		// System.out.println(nodeMap.map);
		LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
		LabeledNode dst = new LabeledNode(edge.getDestination(), edge.getDstLabel());

		THashSet<LabeledNode> srcNeighbor = nodeMap.getNeighbors(src);
		THashSet<LabeledNode> dstNeighbor = nodeMap.getNeighbors(dst);

		SetFunctions<LabeledNode> functions = new SetFunctions<LabeledNode>();
		Set<LabeledNode> common = functions.intersectionSet(srcNeighbor, dstNeighbor);

		// iterate through source neighbors;
		for (LabeledNode t : srcNeighbor) {
			if (!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t, edge, new StreamEdge(src.getVertexId(),
						src.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));
				addSubgraph(triplet);
			}
		}

		// iteration through destination neighbors
		for (LabeledNode t : dstNeighbor) {
			if (!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t, edge, new StreamEdge(dst.getVertexId(),
						dst.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));
				addSubgraph(triplet);
			} else {
				LabeledNode a = src;
				LabeledNode b = dst;
				LabeledNode c = t;
				StreamEdge edgeA = edge;
				StreamEdge edgeB = new StreamEdge(t.getVertexId(), t.getVertexLabel(),
						src.getVertexId(), src.getVertexLabel());
				StreamEdge edgeC = new StreamEdge(t.getVertexId(), t.getVertexLabel(),
						dst.getVertexId(), dst.getVertexLabel());

				Triplet tripletWedge = new Triplet(a, b, c, edgeB, edgeC);
				removeSubgraph(tripletWedge);

				Triplet tripletTriangle = new Triplet(a, b, c, edgeA, edgeB, edgeC);
				addSubgraph(tripletTriangle);

			}
		}

		utility.handleEdgeAddition(edge, nodeMap);
		// System.out.println(counter);
		return false;
	}

	void removeSubgraph(Triplet t) {
		numSubgraph--;
		removeFrequentPattern(t);
	}

	void addSubgraph(Triplet t) {
		addFrequentPattern(t);
		numSubgraph++;
	}

	void addFrequentPattern(Triplet t) {
		Pattern p = new ThreeNodeGraphPattern(t);
		supportCount.add(p);
	}

	void removeFrequentPattern(Triplet t) {
		Pattern p = new ThreeNodeGraphPattern(t);
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
		// This method is not implemented for incremental algorithms
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
