package graphpattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;

import input.StreamEdge;
import struct.Quadriplet;
import topkgraphpattern.Pattern;
import topkgraphpattern.SubgraphType;

public class FourNodeGraphPattern implements Comparable<FourNodeGraphPattern>, Pattern {
	private List<LabeledStreamEdge> labels;
	private SubgraphType type;
	private int numEdges;
	private int maxDegree;

	public FourNodeGraphPattern(Quadriplet t) {
		labels = new ArrayList<LabeledStreamEdge>();
		Set<StreamEdge> labeledEdges = t.getAllEdges();
		for (StreamEdge labeledEdge : labeledEdges) {
			labels.add(new LabeledStreamEdge(labeledEdge.getSrcLabel(), labeledEdge.getDstLabel()));
		}
		labels.sort(new Comparator<LabeledStreamEdge>() {
			@Override
			public int compare(LabeledStreamEdge m1, LabeledStreamEdge m2) {
				return m1.compareTo(m2);
			}
		});
		type = t.getType();
		numEdges = t.getNumEdges();
		maxDegree = t.getMaxDegree();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FourNodeGraphPattern that = (FourNodeGraphPattern) o;
		return numEdges == that.numEdges &&
				maxDegree == that.maxDegree &&
				Objects.equals(labels, that.labels) &&
				type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(labels, type, numEdges, maxDegree);
	}

	@Override
	public String toString() {
		return "FourNodeGraphPattern{" +
				"labels=" + labels +
				", type=" + type +
				", numEdges=" + numEdges +
				", maxDegree=" + maxDegree +
				'}';
	}

	@Override
	public SubgraphType getType() {
		return this.type;
	}

	public void setType(SubgraphType type) {
		this.type = type;
	}

	public int getNumEdges() {
		return this.numEdges;
	}

	public void setNumEdges(int numEdges) {
		this.numEdges = numEdges;
	}

	public int getMaxDegree() {
		return this.maxDegree;
	}

	public void setMaxDegree(int maxDegree) {
		this.maxDegree = maxDegree;
	}

	public List<LabeledStreamEdge> getLabels() {
		return labels;
	}

	public void setLabels(List<LabeledStreamEdge> labels) {
		this.labels = labels;
	}

	@Override
	public int compareTo(FourNodeGraphPattern o) {
		FourNodeGraphPattern p = o;
		if (numEdges != p.numEdges) {
			return numEdges - p.numEdges;
		} else if (!type.equals(p.getType())) {
			return maxDegree - p.getMaxDegree();
		} else if (!this.labels.equals(p.labels)) {
			return -1;
		} else {
			return 0;
		}
	}
}
