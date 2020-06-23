package graphpattern;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang.builder.HashCodeBuilder;

import input.StreamEdge;
import struct.Triplet;
import topkgraphpattern.Pattern;
import topkgraphpattern.SubgraphType;

public class ThreeNodeGraphPattern implements Comparable<ThreeNodeGraphPattern>, Pattern {
	private int label1;
	private int label2;
	private int label3;
	private boolean isWedge;

	public ThreeNodeGraphPattern(Triplet t) {
		if (t.numEdges == 2) {
			isWedge = true;
			StreamEdge edgeA = t.edgeA;
			int sLabel = edgeA.getSrcLabel();
			int tLabel = edgeA.getDstLabel();

			StreamEdge edgeB = t.edgeB;
			int xLabel = edgeB.getSrcLabel();
			int yLabel = edgeB.getDstLabel();

			if (sLabel == xLabel) {
				label1 = sLabel;
				if (tLabel < yLabel) {
					label2 = tLabel;
					label3 = yLabel;
				} else {
					label2 = yLabel;
					label3 = tLabel;
				}
			} else if (sLabel == yLabel) {
				label1 = sLabel;
				if (tLabel < xLabel) {
					label2 = tLabel;
					label3 = xLabel;
				} else {
					label2 = xLabel;
					label3 = tLabel;
				}
			} else if (tLabel == xLabel) {
				label1 = tLabel;
				if (sLabel < yLabel) {
					label2 = sLabel;
					label3 = yLabel;
				} else {
					label2 = yLabel;
					label3 = sLabel;
				}
			} else if (tLabel == yLabel) {
				label1 = tLabel;
				if (sLabel < xLabel) {
					label2 = sLabel;
					label3 = xLabel;

				} else {
					label2 = xLabel;
					label3 = sLabel;
				}
			}

		} else {
			isWedge = false;
			int[] arr = new int[3];

			arr[0] = t.nodeA.getVertexLabel();
			arr[1] = t.nodeB.getVertexLabel();
			arr[2] = t.nodeC.getVertexLabel();

			Arrays.sort(arr);
			label1 = arr[0];
			label2 = arr[1];
			label3 = arr[2];
		}
	}

	public boolean isWedge() {
		return this.isWedge;
	}

	@Override
	public String toString() {
		return "ThreeNodeGraphPattern{" +
				"label1=" + label1 +
				", label2=" + label2 +
				", label3=" + label3 +
				", isWedge=" + isWedge +
				'}';
	}

	public int getLabel1() {
		return label1;
	}

	public void setLabel1(int label1) {
		this.label1 = label1;
	}

	public int getLabel2() {
		return label2;
	}

	public void setLabel2(int label2) {
		this.label2 = label2;
	}

	public int getLabel3() {
		return label3;
	}

	public void setLabel3(int label3) {
		this.label3 = label3;
	}

	public void setWedge(boolean wedge) {
		isWedge = wedge;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ThreeNodeGraphPattern that = (ThreeNodeGraphPattern) o;
		return label1 == that.label1 &&
				label2 == that.label2 &&
				label3 == that.label3 &&
				isWedge == that.isWedge;
	}

	@Override
	public int hashCode() {
		return Objects.hash(label1, label2, label3, isWedge);
	}

	@Override
	public int compareTo(ThreeNodeGraphPattern o) {
		if (this.isWedge != o.isWedge) {
			if (this.isWedge)
				return -1;
			else
				return 1;
		}
		if (this.label1 < o.label1) {
			return -1;
		} else if (this.label1 == o.label1) {
			if (this.label2 < o.label2) {
				return -1;
			} else if (this.label2 == o.label2) {
				return this.label3 - o.label3;
			} else
				return 1;
		} else
			return 1;
	}

	@Override
	public SubgraphType getType() {
		if (this.isWedge) {
			return SubgraphType.WEDGE;
		} else {
			return SubgraphType.TRIANGLE;
		}
	}
}
