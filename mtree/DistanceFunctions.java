package mtree;

import utils.TimeIntervalMR;

/**
 * Some pre-defined implementations of {@linkplain DistanceFunction distance
 * functions}.
 */
public final class DistanceFunctions {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private DistanceFunctions() {
	}

	/**
	 * A {@linkplain DistanceFunction distance function} object that calculates
	 * the distance between two {@linkplain EuclideanCoordinate euclidean
	 * coordinates}.
	 */
	public static final DistanceFunction<TimeIntervalMR> EUCLIDEAN = new DistanceFunction<TimeIntervalMR>() {
		@Override
		public double calculate(TimeIntervalMR coord1, TimeIntervalMR coord2) {
			return Math.sqrt(Math.pow(coord1.get(0) - coord2.get(0), 2) + Math.pow(coord1.get(1) - coord2.get(1), 2));
		}
	};

}
