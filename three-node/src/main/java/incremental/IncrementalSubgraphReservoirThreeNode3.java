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
import utility.AlgorithmZ;
import utility.EdgeHandler;
import utility.ReservoirSampling;
import utility.SetFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IncrementalSubgraphReservoirThreeNode3 implements TopkGraphPatterns {
	NodeMap nodeMap;
	EdgeHandler utility;
	SubgraphReservoir<Triplet> reservoir;
	SupportCount supportCount;
	long N; // total number of subgraphs
	int M; // maximum reservoir size
	int sum;
	AlgorithmZ skipFunction;
	ReservoirSampling<Triplet> sampler; // = new ReservoirSampling<LabeledNeighbor>();
	public IncrementalSubgraphReservoirThreeNode3(int size, int k ) { 
		this.nodeMap = new NodeMap();
		utility = new EdgeHandler();
		reservoir = new SubgraphReservoir<Triplet>();
		N = 0;
		M = size;
		supportCount = new MapSupportCount();
		sum = 0;
		skipFunction = new AlgorithmZ(M);
		sampler = new ReservoirSampling<Triplet>();
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

		List<Triplet> list = new ArrayList<Triplet>();

		for(LabeledNode t: srcNeighbor) {
			if(!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(src.getVertexId(), src.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));

				list.add(triplet);
			}
		}

		for(LabeledNode t: dstNeighbor) {
			if(!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(dst.getVertexId(), dst.getVertexLabel(), t.getVertexId() , t.getVertexLabel()));
				list.add(triplet);
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
				}

			}
		}

		int i = 0 ;
		int W = list.size();
		//System.out.println("W " + W);
		if(W> 0) {
			while(sum < W) {
				i++;
				int zrs = skipFunction.apply((int)N);
				N = N+zrs+1;
				sum = sum+zrs+1;
			}
			//System.out.println("i equals "+ i);
			List<Triplet> sample = sampler.selectKItems(list, i);

			//System.out.println("sample size " + sample.size());
			for(Triplet t: sample) {

				if(reservoir.size() >= M) {
					Triplet temp = reservoir.getRandom();
					reservoir.remove(temp);
					removeFrequentPattern(temp);
				}
				reservoir.add(t); 
				addFrequentPattern(t);

			}
			sum = sum-W;
		}

		utility.handleEdgeAddition(edge, nodeMap);
		//System.out.println(reservoir.size() + "  N " + N);
		return false;
	}
	@Override
	public boolean removeEdge(StreamEdge edge) {
		return false;
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
