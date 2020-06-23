package graphpattern;

import java.util.Objects;

public class LabeledStreamEdge implements Comparable<LabeledStreamEdge>{
	private int labelA;
	private int labelB;
	
	LabeledStreamEdge(int labelA, int labelB) {
		if(labelA < labelB) {
			this.labelA = labelA;
			this.labelB = labelB;
		} else {
			this.labelB = labelA;
			this.labelA = labelB;
		}
	}

	@Override
	public String toString() {
		return "LabeledStreamEdge{" +
				"labelA=" + labelA +
				", labelB=" + labelB +
				'}';
	}

	public int getLabelA() {
		return labelA;
	}

	public void setLabelA(int labelA) {
		this.labelA = labelA;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LabeledStreamEdge that = (LabeledStreamEdge) o;
		return labelA == that.labelA &&
				labelB == that.labelB;
	}

	@Override
	public int hashCode() {
		return Objects.hash(labelA, labelB);
	}

	@Override
	public int compareTo(LabeledStreamEdge o) {
		if(this.labelA < o.labelA) {
			return -1;
		} else if (this.labelA == o.labelA) { 
			if(this.labelB < o.labelB) {
				return -1;
			} else if (this.labelB == o.labelB) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}
}
