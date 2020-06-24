package incremental;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import graphpattern.ThreeNodeGraphPattern;
import input.StreamEdge;
import reservoir.SubgraphReservoir;
import struct.LabeledNode;
import struct.NodeMap;
import struct.Triplet;
import support.MapSupportCount;
import support.SupportCount;
import topkgraphpattern.Pattern;
import topkgraphpattern.TopkGraphPatterns;
import utility.EdgeHandler;
import utility.SetFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IncrementalSubgraphReservoirThreeNode implements TopkGraphPatterns {
	NodeMap nodeMap;
	EdgeHandler utility;
	SubgraphReservoir<Triplet> reservoir;
	SupportCount supportCount;
	
	long numEdges; // total number of subgraphs
	int reservoirSize; // maximum reservoir size
	public IncrementalSubgraphReservoirThreeNode(int size, int k ) { 
		this.nodeMap = new NodeMap();
		utility = new EdgeHandler();
		reservoir = new SubgraphReservoir<Triplet>();
		numEdges = 0;
		reservoirSize = size;
		supportCount = new MapSupportCount();
		
	}

	@Override
	public boolean addEdge(StreamEdge edge) {
		if(nodeMap.contains(edge)) {
			return false;
		}
		//System.out.println("+" + edge);
		LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
		LabeledNode dst = new LabeledNode(edge.getDestination(),edge.getDstLabel());


		THashSet<LabeledNode> srcNeighbor = nodeMap.getNeighbors(src);
		THashSet<LabeledNode> dstNeighbor = nodeMap.getNeighbors(dst);
		
		//System.out.println("src neighbor" + srcNeighbor);
		//System.out.println("dst neighbor " + dstNeighbor);

		SetFunctions<LabeledNode> functions = new SetFunctions<LabeledNode>();
		Set<LabeledNode> common = functions.intersectionSet(srcNeighbor, dstNeighbor);

		//System.out.println("common " +  common);

		for(LabeledNode t: srcNeighbor) {
			if(!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(src.getVertexId(), src.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));
				addSubgraph(triplet);
			}
		}

		for(LabeledNode t: dstNeighbor) {
			if(!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(dst.getVertexId(), dst.getVertexLabel(), t.getVertexId() , t.getVertexLabel()));
				addSubgraph(triplet);
			}else {
				LabeledNode a = src;
				LabeledNode b = dst;
				LabeledNode c = t;
				StreamEdge edgeA = edge;
				StreamEdge edgeB = new StreamEdge(t.getVertexId() , t.getVertexLabel(), src.getVertexId(), src.getVertexLabel());
				StreamEdge edgeC = new StreamEdge(t.getVertexId(), t.getVertexLabel(), dst.getVertexId(), dst.getVertexLabel());

				Triplet tripletWedge = new Triplet(a, b, c, edgeB, edgeC );
				if(reservoir.contains(tripletWedge)) {
					Triplet tripletTriangle = new Triplet(a, b, c,edgeA, edgeB, edgeC );
					replaceSubgraphs(tripletWedge, tripletTriangle);
					//System.out.println("triangle added" + tripletTriangle);
				}

			}
		}
		utility.handleEdgeAddition(edge, nodeMap);
		//System.out.println(reservoir.size() + "  N " + N);
		return false;
	}
	@Override
	public boolean removeEdge(StreamEdge edge) {
				return false;
	}
	void removeSubgraph(Triplet t) {
		if(reservoir.contains(t)) {
			//System.out.println("remove called from remove subgraph");
			reservoir.remove(t);
			removeFrequentPattern(t);
		}
		numEdges--;
	}

	void addSubgraph(Triplet t) {
		numEdges++;
		
		boolean flag = false;
			if(reservoir.size() <= reservoirSize ) {
				flag = true;
			}else if (Math.random() < (reservoirSize/(double)numEdges)) {
				flag = true;
				//System.out.println("remove called from add subgraph");
				Triplet temp = reservoir.getRandom();
				reservoir.remove(temp);
				removeFrequentPattern(temp);
			}
		

		if(flag) {
			reservoir.add(t); 
			addFrequentPattern(t);
			//System.out.println("reservoir size after add method " + reservoir.size());
		}
	}

	//remove a and add b
	void replaceSubgraphs(Triplet a, Triplet b) {
		reservoir.remove(a);
		removeFrequentPattern(a);
		reservoir.add(b);
		addFrequentPattern(b);
		
	}
	
	void addFrequentPattern(Triplet t) {
		ThreeNodeGraphPattern p = new ThreeNodeGraphPattern(t);
		supportCount.add(p);
	}
	
	void removeFrequentPattern(Triplet t) {
		ThreeNodeGraphPattern p = new ThreeNodeGraphPattern(t);
		supportCount.remove(p);
	}
	
	
	@Override
	public THashMap<Pattern, Long> getFrequentPatterns() {
		return this.supportCount.getPatternCount();
	}
	@Override
	public THashMap<Pattern, Long> correctEstimates() {
		THashMap<Pattern, Long> correctFrequentPatterns = new THashMap<Pattern, Long>();
		double correctFactor = correctFactor();
		THashMap<Pattern, Long> frequentPatterns = this.supportCount.getPatternCount();
		List<Pattern> patterns = new ArrayList<Pattern>(frequentPatterns.keySet());
		for(Pattern p: patterns) {
			long count = frequentPatterns.get(p);
			double value = count*correctFactor;
			correctFrequentPatterns.put(p, (long)value);
		}
		return correctFrequentPatterns;
	}
	private double correctFactor() { 
		return Math.max(1, ((double)numEdges/reservoirSize));
	}
	
	@Override
	public long getNumberofSubgraphs() {
		return numEdges;
	}

	@Override
	public int getCurrentReservoirSize() {
		return reservoir.size();
	}
}
