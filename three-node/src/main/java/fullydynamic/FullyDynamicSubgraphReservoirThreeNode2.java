package fullydynamic;

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
import utility.AlgorithmD;
import utility.AlgorithmZ;
import utility.BottomKSketch;
import utility.EdgeHandler;
import utility.ReservoirSampling;
import utility.SetFunctions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class FullyDynamicSubgraphReservoirThreeNode2 implements TopkGraphPatterns {
	NodeMap nodeMap;
	EdgeHandler utility;
	AdvancedSubgraphReservoir<Triplet> reservoir;
	SupportCount supportCount;
	NodeBottomK nodeBottomK;
	int N; // total number of subgraphs
	int M; // maximum reservoir size
	int Ncurrent;
	public int c1;
	public int c2;
	int sum;
	AlgorithmZ skipRS;
	AlgorithmD skipRP;
	int Zprime;
	Random rand;
	ReservoirSampling<LabeledNode> sampler;

	public FullyDynamicSubgraphReservoirThreeNode2(int size, int k ) { 
		this.nodeMap = new NodeMap();
		this.nodeBottomK = new NodeBottomK();
		this.rand = new Random();
		this.rand.setSeed(17);
		utility = new EdgeHandler();
		reservoir = new AdvancedSubgraphReservoir<Triplet>();
		N = 0;
		M = size;
		c1=0;
		Ncurrent = 0 ;
		c2=0;
		sum = 0;
		Zprime=-1;
		supportCount = new MapSupportCount();
		skipRS = new AlgorithmZ(M);
		//skipRP = new AlgorithmD();
		sampler = new ReservoirSampling<LabeledNode>();
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

		//BottomKSketch<LabeledNode> srcSketch = nodeBottomK.getSketch(src);
		//BottomKSketch<LabeledNode> dstSketch = nodeBottomK.getSketch(dst);
		//int W = srcSketch.unionImprovedCardinality(dstSketch)-srcSketch.intersectionImprovedCardinality(dstSketch);
		SetFunctions<LabeledNode> fun = new SetFunctions<LabeledNode>();
		THashSet<LabeledNode> union = fun.unionSet(srcNeighbor, dstNeighbor);
		int W = union.size()-fun.intersection(srcNeighbor, dstNeighbor);
		//System.out.println("W "+ W + " " + srcNeighbor + " "  + dstNeighbor);

		if(c1+c2 == 0) {
			//System.out.println("W "  + W);
			if(W> 0) {
				int i = 0 ;
				while(sum <W) {
					i++;
					int zrs = skipRS.apply(N);
					N = N+zrs+1;
					Ncurrent = Ncurrent+zrs+1;
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
						//count++;
					}else {
						set.add(randomVertex);
						THashSet<LabeledNode> randomVertexNeighbor = nodeMap.getNodeNeighbors(randomVertex);
						if(randomVertexNeighbor.contains(src) && randomVertexNeighbor.contains(dst)) {
							//triangle -> hence, rejected!!!!!
							count++;
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
		}else {
			int count = 0 ;
			HashSet<LabeledNode> set = new HashSet<LabeledNode>();
			while(count < W) {
				LabeledNode randomVertex = getRandomNeighbor(srcNeighbor, dstNeighbor);

				if(randomVertex == null) {
					break;
				}	else if (set.contains(randomVertex)) {
					count++;
				} else {
					set.add(randomVertex);
					//System.out.println(srcNeighbor + " " + dstNeighbor);
					THashSet<LabeledNode> randomVertexNeighbor = nodeMap.getNodeNeighbors(randomVertex);
					if(randomVertexNeighbor.contains(src) && randomVertexNeighbor.contains(dst)) {
						//triangle -> hence, rejected!!!!!
						count++;
					}else if (randomVertexNeighbor.contains(src)) {
						Triplet triplet = new Triplet(src, dst, randomVertex,edge, new StreamEdge(src.getVertexId(), src.getVertexLabel(), randomVertex.getVertexId(), randomVertex.getVertexLabel()));
						addSubgraph(triplet);
						count++;
					}else {
						Triplet triplet = new Triplet(src, dst, randomVertex,edge, new StreamEdge(dst.getVertexId(), dst.getVertexLabel(), randomVertex.getVertexId(), randomVertex.getVertexLabel()));
						addSubgraph(triplet);
						count++;
					}
				}
			}

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
	/*void addSubgraph(Triplet t) {
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
			if(Zprime < 0) {
				Zprime = skipRP.vitter_d_skip(c1,c1+c2);
			}

			if(Zprime == 0) {
				flag = true;
				c1--;
			}else {
				c2--;
			}

			Zprime--;
		}

		if(flag) {
			reservoir.add(t); 
			addFrequentPattern(t);
			//System.out.println("reservoir size after add method " + reservoir.size());
		}
	}
	 */
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

	public LabeledNode getRandomNeighbor(THashSet<LabeledNode> srcNeighbor, THashSet<LabeledNode> dstNeighbor) {
		int d_u = srcNeighbor.size();
		int d_v = dstNeighbor.size();

		if(d_u+d_v == 0) {
			return null;
		}

		double value = d_u/(double)(d_u+d_v);
		double ran = Math.random();
		if(ran <= value) {
			ArrayList<LabeledNode> list = new ArrayList<LabeledNode>(srcNeighbor);
			return list.get(rand.nextInt(list.size()));
		}else {
			ArrayList<LabeledNode> list = new ArrayList<LabeledNode>(dstNeighbor);
			return list.get(rand.nextInt(list.size()));
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

		nodeBottomK.removeEdge(src, dst);


		THashSet<LabeledNode> srcNeighbor = nodeMap.getNeighbors(src);
		THashSet<LabeledNode> dstNeighbor = nodeMap.getNeighbors(dst);

		BottomKSketch<LabeledNode> srcSketch = nodeBottomK.getSketch(src);
		BottomKSketch<LabeledNode> dstSketch = nodeBottomK.getSketch(dst);
		int W = srcSketch.unionImprovedCardinality(dstSketch)-srcSketch.intersectionImprovedCardinality(dstSketch);

		//SetFunctions<LabeledNeighbor> fun = new SetFunctions<LabeledNeighbor>();
		//HashSet<LabeledNeighbor> union = fun.unionSet(srcNeighbor, dstNeighbor);
		//int W = union.size()-fun.intersection(srcNeighbor, dstNeighbor);
		Ncurrent-=W;

		//remove all the wedges from the graphs
		THashSet<Triplet> candidateWedges = reservoir.getAllSubgraphs(src);
		ArrayList<Triplet> wedges = new ArrayList<Triplet>();
		for(Triplet t: candidateWedges) {
			if((t.edgeA.equals(edge) || t.edgeB.equals(edge)) && !t.isTriangle()) {
				wedges.add(t);
			}
		}

		if(wedges.size() >0) {
			int count = wedges.size();
			for(Triplet t: wedges) {
				reservoir.remove(t);
				removeFrequentPattern(t);
				Zprime = -1;
			}
			c1+=count;
			c2+= (W-count);
		}
		//finished removing all the wedges
		//System.out.println(reservoir.size());

		//update all triangles in the reservoir and replace them with the wedges
		THashSet<Triplet> candidateTriangles = reservoir.getAllSubgraphs(src);
		ArrayList<Triplet> triangles = new ArrayList<Triplet>();
		//System.out.println("size "  + candidateTriangles.size());
		for(Triplet t: candidateTriangles) {
			if(t.isTriangle()) {
				if((t.nodeA.equals(dst) || t.nodeB.equals(dst) || t.nodeC.equals(dst))) {
					triangles.add(t);
				}
			}
		}
		if(triangles.size() > 0) {
			for(Triplet t: triangles) {
				Triplet newWedge = null;
				if(edge.equals(t.edgeA)) {
					newWedge = new Triplet(t.nodeA,t.nodeB,t.nodeC,t.edgeB, t.edgeC);
				}else if (edge.equals(t.edgeB)) {
					newWedge = new Triplet(t.nodeA,t.nodeB,t.nodeC,t.edgeA, t.edgeC);
				}else {
					newWedge = new Triplet(t.nodeA,t.nodeB,t.nodeC,t.edgeA, t.edgeB);
				}
				replaceSubgraphs(t, newWedge);
			}
		}
		//end updating triangles
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
