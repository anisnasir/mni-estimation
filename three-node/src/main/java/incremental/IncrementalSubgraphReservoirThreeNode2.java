package incremental;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import graphpattern.ThreeNodeGraphPattern;
import input.StreamEdge;
import reservoir.AdvancedSubgraphReservoir;
import struct.LabeledNode;
import struct.NodeBottomK;
import struct.NodeMap;
import struct.Triplet;
import support.MapSupportCount;
import support.SupportCount;
import topkgraphpattern.Pattern;
import topkgraphpattern.TopkGraphPatterns;
import utility.AlgorithmZ;
import utility.EdgeHandler;
import utility.SetFunctions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class IncrementalSubgraphReservoirThreeNode2 implements TopkGraphPatterns {
	NodeMap nodeMap;
	NodeBottomK nodeBottomK;
	EdgeHandler utility;
	AdvancedSubgraphReservoir<Triplet> reservoir;
	Random rand;

	SupportCount supportCount;
	long N; // total number of subgraphs
	int M; // maximum reservoir size
	int sum;
	AlgorithmZ skipRS;
	public IncrementalSubgraphReservoirThreeNode2(int size, int k ) { 
		this.nodeMap = new NodeMap();
		this.nodeBottomK = new NodeBottomK();
		rand = new Random();
		utility = new EdgeHandler();
		reservoir = new AdvancedSubgraphReservoir<Triplet>();
		N = 0;
		M = size;
		supportCount = new MapSupportCount();
		sum = 0;
		skipRS = new AlgorithmZ(M);
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

		//replaces the existing wedges in the reservoir with the triangles
		THashSet<Triplet> candidateTriangles = reservoir.getAllSubgraphs(src);
		ArrayList<Triplet> oldWedges = new ArrayList<Triplet>();
		//System.out.println("size "  + candidateTriangles.size());
		for(Triplet t: candidateTriangles) {
			if((t.nodeA.equals(dst) || t.nodeB.equals(dst) || t.nodeC.equals(dst)) && !t.isTriangle()) {
				oldWedges.add(t);
			}
		}
		if(oldWedges.size() > 0) {
			for(Triplet t: oldWedges) {
				Triplet newTriangle = new Triplet(t.nodeA,t.nodeB,t.nodeC,t.edgeA, t.edgeB,edge);
				replaceSubgraphs(t, newTriangle);
			}
		}

		//BottomKSketch<LabeledNeighbor> srcSketch = nodeBottomK.getSketch(src);
		//BottomKSketch<LabeledNeighbor> dstSketch = nodeBottomK.getSketch(dst);
		//int W = srcSketch.unionImprovedCardinality(dstSketch)-srcSketch.intersectionImprovedCardinality(dstSketch);
		SetFunctions<LabeledNode> fun = new SetFunctions<LabeledNode>();
		THashSet<LabeledNode> union = fun.unionSet(srcNeighbor, dstNeighbor);
		int W = union.size()-fun.intersection(srcNeighbor, dstNeighbor);
		//System.out.println("W "+ W + " " + srcNeighbor + " "  + dstNeighbor);

		//System.out.println("W "  + W);
		if(W> 0) {
			
			int i = 0 ;
			while(sum <W) {
				i++;
				int zrs = skipRS.apply((int)N);
				N = N+zrs+1;
				sum = sum+zrs+1;
			}
			//added i wedges to the reservoir
			//we would randomly pick a vertex from the neighborhood of src and dst
			//and add it to the reservoir
			//System.out.println("i " + i + " W " + W);
			THashSet<LabeledNode> set = new THashSet<LabeledNode>();
			int count = 0 ;
			while(count < i) {
				LabeledNode randomVertex = getRandomNeighbor(srcNeighbor, dstNeighbor);
				if(randomVertex == null) {
					break;
				}else if (set.contains(randomVertex)) {
					//wedge already added
					
				}else {
					set.add(randomVertex);
					THashSet<LabeledNode> randomVertexNeighbor = nodeMap.getNodeNeighbors(randomVertex);
					if(randomVertexNeighbor.contains(src) && randomVertexNeighbor.contains(dst)) {
						//triangle -> hence, rejected!!!!!
					}else if (randomVertexNeighbor.contains(src)) {
						Triplet triplet = new Triplet(src, dst, randomVertex,edge, new StreamEdge(src.getVertexId(), src.getVertexLabel(), randomVertex.getVertexId(), randomVertex.getVertexLabel()));
						addToReservoir(triplet);
						count++;
					}else {
						Triplet triplet = new Triplet(src, dst, randomVertex,edge, new StreamEdge(dst.getVertexId(), dst.getVertexLabel(), randomVertex.getVertexId(), randomVertex.getVertexLabel()));
						addToReservoir(triplet);
						count++;
					}
				}
			}
			sum = sum-W;
		}

		utility.handleEdgeAddition(edge, nodeMap);
		//System.out.println(reservoir.size() + "  N " + N);
		nodeBottomK.addEdge(src, dst);
		return false;
	}
	void addToReservoir(Triplet triplet) { 
		if(reservoir.size() >= M) {
			Triplet temp = reservoir.getRandom();
			reservoir.remove(temp);
			removeFrequentPattern(temp);
		}
		reservoir.add(triplet); 
		addFrequentPattern(triplet);

	}
	public HashSet<LabeledNode> getNeighbors(HashSet<LabeledNode> randomVertexNeighborWithEdgeLabels) {
		HashSet<LabeledNode> results = new HashSet<LabeledNode>();
		for(LabeledNode a: randomVertexNeighborWithEdgeLabels) {
			results.add(a);
		}
		return results;
	}
	@Override
	public boolean removeEdge(StreamEdge edge) {
		return false;
	}

	public LabeledNode getRandomNeighbor(THashSet<LabeledNode> srcNeighbor, THashSet<LabeledNode> dstNeighbor) {
		int d_u = srcNeighbor.size();
		int d_v = dstNeighbor.size();

		if(d_u+d_v == 0) {
			return null;
		}

		double value = d_u/(double)(d_u+d_v);
		if(Math.random() < value) {
			//select neighbor of u or src
			ArrayList<LabeledNode> list = new ArrayList<LabeledNode>(srcNeighbor);
			return list.get(rand.nextInt(list.size()));
		}else {
			//select a neighbor of v or dst
			ArrayList<LabeledNode> list = new ArrayList<LabeledNode>(dstNeighbor);
			return list.get(rand.nextInt(list.size()));
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
		return Math.max(1, ((double)N/M));
	}

	@Override
	public long getNumberofSubgraphs() {
		return N;
	}

	@Override
	public int getCurrentReservoirSize() {
		return reservoir.size();
	}
}
