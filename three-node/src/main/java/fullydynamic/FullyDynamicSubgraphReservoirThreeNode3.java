package fullydynamic;

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
import utility.AlgorithmD;
import utility.AlgorithmZ;
import utility.EdgeHandler;
import utility.ReservoirSampling;
import utility.SetFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FullyDynamicSubgraphReservoirThreeNode3 implements TopkGraphPatterns {
	NodeMap nodeMap;
	EdgeHandler utility;
	SubgraphReservoir<Triplet> reservoir;
	SupportCount supportCount;
	int N; // total number of subgraphs
	int M; // maximum reservoir size
	int Ncurrent;
	int c1;
	int c2;
	int sum;
	AlgorithmZ skipRS;
	AlgorithmD skipRP;
	int sum2;
	ReservoirSampling<Triplet> sampler; // = new ReservoirSampling<LabeledNeighbor>();
	public FullyDynamicSubgraphReservoirThreeNode3(int size, int k ) { 
		this.nodeMap = new NodeMap();
		utility = new EdgeHandler();
		reservoir = new SubgraphReservoir<Triplet>();
		N = 0;
		M = size;
		c1=0;
		Ncurrent = 0 ;
		c2=0;
		sum = 0;
		sum2=0;
		supportCount = new MapSupportCount();
		skipRS = new AlgorithmZ(M);
		skipRP = new AlgorithmD();
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

		if(c1+c2 == 0) {
			int i = 0 ;
			int W = list.size();
			//System.out.println("list " + list);
			if(W> 0) {
				while(sum < W) {
					i++;
					int zrs = skipRS.apply(N);
					N = N+zrs+1;
					Ncurrent= Ncurrent+zrs+1;
					sum = sum+zrs+1;
				}
				//System.out.println("i equals "+ i);
				List<Triplet> sample = sampler.selectKItems(list, i);

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
		}else {
			for(Triplet t: list) {
				addSubgraph(t);
			}
		}
		utility.handleEdgeAddition(edge, nodeMap);
		//System.out.println(reservoir.size() + "  N " + N);
		return false;
	}
	void addSubgraph(Triplet t) {
		N++;
		Ncurrent++;
		
		boolean flag = false;
		if (c1+c2 ==0) {
			if(reservoir.size() < M ) {
				flag = true;
			}else if (Math.random() < (M/(double)N)) {
				flag = true;
				//System.out.println("remove called from add subgraph");
				Triplet temp = reservoir.getRandom();
				reservoir.remove(temp);
				removeFrequentPattern(temp);
			}
		}else {
			int d = c1+c2;
			if (Math.random() < (c1/(double)(d))) {
				flag = true;
				c1--;
			}else {
				c2--;
			}
		}

		if(flag) {
			reservoir.add(t); 
			addFrequentPattern(t);
			//System.out.println("reservoir size after add method " + reservoir.size());
		}
	}
	@Override
	public boolean removeEdge(StreamEdge edge) {
		//System.out.println("-" + edge);
		if(!nodeMap.contains(edge)) {
			return false;
		}
		utility.handleEdgeDeletion(edge, nodeMap);

		LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
		LabeledNode dst = new LabeledNode(edge.getDestination(),edge.getDstLabel());


		THashSet<LabeledNode> srcNeighbor = nodeMap.getNeighbors(src);
		THashSet<LabeledNode> dstNeighbor = nodeMap.getNeighbors(dst);

		SetFunctions<LabeledNode> functions = new SetFunctions<LabeledNode>();
		Set<LabeledNode> common = functions.intersectionSet(srcNeighbor, dstNeighbor);

		List<Triplet> list = new ArrayList<Triplet>();
		
		for(LabeledNode t: srcNeighbor) {
			if(!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(src.getVertexId(), src.getVertexLabel(), t.getVertexId() , t.getVertexLabel()));
				list.add(triplet);
			}
		}

		for(LabeledNode t: dstNeighbor) {
			if(!common.contains(t)) {
				Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(dst.getVertexId(),dst.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));
				list.add(triplet);
			}else {
				LabeledNode a = src;
				LabeledNode b = dst;
				LabeledNode c = t;
				StreamEdge edgeA = edge;
				StreamEdge edgeB = new StreamEdge(c.getVertexId(), c.getVertexLabel(), src.getVertexId(), src.getVertexLabel());
				StreamEdge edgeC = new StreamEdge(c.getVertexId(), c.getVertexLabel(), dst.getVertexId(), dst.getVertexLabel());

				Triplet tripletWedge = new Triplet(a, b, c, edgeB, edgeC );
				Triplet tripletTriangle = new Triplet(a, b, c,edgeA, edgeB, edgeC );
				if(reservoir.contains(tripletTriangle))
					replaceSubgraphs(tripletTriangle, tripletWedge);

			}
		}
		
		for(Triplet wedge: list) {
			if(reservoir.contains(wedge)) {
				reservoir.remove(wedge);
				removeFrequentPattern(wedge);
				c1++;
			}else {
				c2++;
			}
			Ncurrent--;
		}

		//System.out.println(reservoir.size());
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
		return Math.max(1, ((double)Ncurrent/M));
	}

	@Override
	public long getNumberofSubgraphs() {
		return Ncurrent;
	}

	@Override
	public int getCurrentReservoirSize() {
		return reservoir.size();
	}
}
