package mtree;

import java.util.Set;

import utils.TimeIntervalMR;

public class MTreeClass extends MTree {

	private static final PromotionFunction<TimeIntervalMR> nonRandomPromotion = new PromotionFunction<TimeIntervalMR>() {
		@Override
		public Pair<TimeIntervalMR> process(Set<TimeIntervalMR> dataSet,
				DistanceFunction<? super TimeIntervalMR> distanceFunction) {
			return Utils.minMax(dataSet);
			// TimeIntervalMR[] pair = new TimeIntervalMR[2];
			// int i = 0;
			// for (TimeIntervalMR data : dataSet) {
			// pair[i] = data;
			// i++;
			// if (i > 1)
			// break;
			// }
			// return new Pair<>(pair[0], pair[1]);
			// List<TimeIntervalMR> promotedList = Utils.randomSample(dataSet, 2);
			// return new Pair<TimeIntervalMR>(promotedList.get(0), promotedList.get(1));
		}
	};

	public MTreeClass() {
		super(10, DistanceFunctions.EUCLIDEAN,
				new ComposedSplitFunction<TimeIntervalMR>(
						nonRandomPromotion,
						new PartitionFunctions.BalancedPartition<TimeIntervalMR>()));
	}

	public void add(TimeIntervalMR data) {
		super.add(data);
		// _check();
	}

	public boolean remove(TimeIntervalMR data) {
		boolean result = super.remove(data);
		// _check();
		return result;
	}

	DistanceFunction<? super TimeIntervalMR> getDistanceFunction() {
		return distanceFunction;
	}
};
