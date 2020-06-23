package input;

import java.io.Serializable;
import java.util.Objects;

public class StreamEdge implements Serializable, Comparable<StreamEdge> {
	private static final long serialVersionUID = -3733214465018614013L;
	private int src;
	private int srcLabel;
	private int dest;
	private int dstLabel;

	public StreamEdge(int src, int srcLabel, int dest, int dstLabel) {
		if (src < dest) {
			this.src = src;
			this.srcLabel = srcLabel;
			this.dest = dest;
			this.dstLabel = dstLabel;
		} else {
			this.src = dest;
			this.srcLabel = dstLabel;
			this.dest = src;
			this.dstLabel = srcLabel;
		}
	}

	public int getSrcLabel() {
		return srcLabel;
	}

	public void setSrcLabel(int srcLabel) {
		this.srcLabel = srcLabel;
	}

	public int getDstLabel() {
		return dstLabel;
	}

	public void setDstLabel(int dstLabel) {
		this.dstLabel = dstLabel;
	}

	public int getSource() {
		return this.src;
	}

	public int getDestination() {
		return this.dest;
	}

	@Override
	public int compareTo(StreamEdge o) {
		if (src < o.src) {
			return -1;
		} else if (src == o.src) {
			if (dest < o.dest) {
				return -1;
			} else if (dest == o.dest) {
				if (srcLabel < o.srcLabel) {
					return -1;
				} else if (srcLabel == o.srcLabel) {
					if (dstLabel < o.dstLabel) {
						return -1;
					} else if(dstLabel == o.dstLabel) {
						return 0;
					}
				}

			}
		}
		return 1;
	}

	@Override
	public String toString() {
		return "StreamEdge{" +
				"src=" + src +
				", srcLabel=" + srcLabel +
				", dest=" + dest +
				", dstLabel=" + dstLabel +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StreamEdge that = (StreamEdge) o;
		return src == that.src &&
				srcLabel == that.srcLabel &&
				dest == that.dest &&
				dstLabel == that.dstLabel;
	}

	@Override
	public int hashCode() {
		return Objects.hash(src, srcLabel, dest, dstLabel);
	}
}
