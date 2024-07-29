package utils;

import java.util.Arrays;

import mtree.DistanceFunctions.EuclideanCoordinate;

/**
 * circle data
 */
public class Data implements EuclideanCoordinate, Comparable<Data> {

	public Ellipse bead = null;
	public double[] values;
	public double radius;

	public Data(Ellipse bead) {
		this.bead = bead;
		this.values = bead.center;
		this.radius = bead.a;
	}

	@Override
	public int dimensions() {
		return values.length;
	}

	@Override
	public double get(int index) {
		return values[index];
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Data) {
			Data that = (Data) obj;
			if (this.dimensions() != that.dimensions()) {
				return false;
			}
			if (this.hashCode() != that.hashCode()) {
				return false;
			}
			for (int i = 0; i < this.dimensions(); i++) {
				if (this.values[i] != that.values[i]) {
					return false;
				}
			}
			if (bead != that.bead)
				return false;
			if (this.bead.objectID != that.bead.objectID) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(Data that) {
		int dimensions = Math.min(this.dimensions(), that.dimensions());
		for (int i = 0; i < dimensions; i++) {
			double v1 = this.values[i];
			double v2 = that.values[i];
			if (v1 > v2) {
				return +1;
			}
			if (v1 < v2) {
				return -1;
			}
		}

		if (this.dimensions() > dimensions) {
			return +1;
		}

		if (that.dimensions() > dimensions) {
			return -1;
		}

		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Arrays.toString(values) + " raidus: " + radius + " a:  " + bead.a + " b: " + bead.b;
	}

}
