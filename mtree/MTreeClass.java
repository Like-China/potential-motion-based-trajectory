package mtree;

import java.util.Set;
import utils.Data;
import utils.Pair;

public class MTreeClass extends MTree {

	private static final PromotionFunction<Data> nonRandomPromotion = new PromotionFunction<Data>() {
		@Override
		public Pair<Data> process(Set<Data> dataSet, DistanceFunction<? super Data> distanceFunction) {
			// return Utils.minMax(dataSet);
			Data[] pair = new Data[2];
			int i = 0;
			for (Data data : dataSet) {
				pair[i] = data;
				i++;
				if (i > 1)
					break;
			}
			return new Pair<>(pair[0], pair[1]);
			// List<Data> promotedList = Utils.randomSample(dataSet, 2);
			// return new Pair<Data>(promotedList.get(0), promotedList.get(1));
		}
	};

	public MTreeClass() {
		super(10, DistanceFunctions.EUCLIDEAN,
				new ComposedSplitFunction<Data>(
						nonRandomPromotion,
						new PartitionFunctions.BalancedPartition<Data>()));
	}

	public void add(Data data) {
		super.add(data);
		// _check();
	}

	public boolean remove(Data data) {
		boolean result = super.remove(data);
		// _check();
		return result;
	}

	DistanceFunction<? super Data> getDistanceFunction() {
		return distanceFunction;
	}
};
