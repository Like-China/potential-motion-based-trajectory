package mtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.TimeIntervalMR;

/**
 * Some pre-defined implementations of {@linkplain PartitionFunction partition
 * functions}.
 */
public final class PartitionFunctions {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PartitionFunctions() {
	}

	/**
	 * A {@linkplain PartitionFunction partition function} that tries to
	 * distribute the data objects equally between the promoted data objects,
	 * associating to each promoted data objects the nearest data objects.
	 * 
	 * @param <TimeIntervalMR> The type of the data objects.
	 */
	public static class BalancedPartition<TimeIntervalMR> implements PartitionFunction<TimeIntervalMR> {

		/**
		 * Processes the balanced partition.
		 * 
		 * <p>
		 * The algorithm is roughly equivalent to this:
		 * 
		 * <pre>
		 *     While dataSet is not Empty:
		 *         X := The object in dataSet which is nearest to promoted.<b>first</b>
		 *         Remove X from dataSet
		 *         Add X to result.<b>first</b>
		 *         
		 *         Y := The object in dataSet which is nearest to promoted.<b>second</b>
		 *         Remove Y from dataSet
		 *         Add Y to result.<b>second</b>
		 *         
		 *     Return result
		 * </pre>
		 * 
		 * @see mtree.PartitionFunction#process(mtree.Pair, java.util.Set,
		 *      mtree.DistanceFunction)
		 */
		@Override
		public Pair<Set<TimeIntervalMR>> process(
				final Pair<TimeIntervalMR> promoted,
				Set<TimeIntervalMR> dataSet,
				final DistanceFunction distanceFunction) {
			List<TimeIntervalMR> queue1 = new ArrayList<TimeIntervalMR>(dataSet);
			// Sort by distance to the first promoted data
			Collections.sort(queue1, new Comparator<TimeIntervalMR>() {
				@Override
				public int compare(TimeIntervalMR data1, TimeIntervalMR data2) {
					double distance1 = distanceFunction.calculate(data1, promoted.first);
					double distance2 = distanceFunction.calculate(data2, promoted.first);
					return Double.compare(distance1, distance2);
				}
			});

			List<TimeIntervalMR> queue2 = new ArrayList<TimeIntervalMR>(dataSet);
			// Sort by distance to the second promoted data
			Collections.sort(queue2, new Comparator<TimeIntervalMR>() {
				@Override
				public int compare(TimeIntervalMR data1, TimeIntervalMR data2) {
					double distance1 = distanceFunction.calculate(data1, promoted.second);
					double distance2 = distanceFunction.calculate(data2, promoted.second);
					return Double.compare(distance1, distance2);
				}
			});

			Pair<Set<TimeIntervalMR>> partitions = new Pair<Set<TimeIntervalMR>>(new HashSet<TimeIntervalMR>(),
					new HashSet<TimeIntervalMR>());

			int index1 = 0;
			int index2 = 0;

			while (index1 < queue1.size() || index2 != queue2.size()) {
				while (index1 < queue1.size()) {
					TimeIntervalMR data = queue1.get(index1++);
					if (!partitions.second.contains(data)) {
						partitions.first.add(data);
						break;
					}
				}

				while (index2 < queue2.size()) {
					TimeIntervalMR data = queue2.get(index2++);
					if (!partitions.first.contains(data)) {
						partitions.second.add(data);
						break;
					}
				}
			}

			return partitions;
		}
	}
}
