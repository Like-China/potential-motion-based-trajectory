package mtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Some pre-defined implementations of {@linkplain PromotionFunction promotion
 * functions}.
 */
public final class PromotionFunctions {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PromotionFunctions() {
	}

	/**
	 * A {@linkplain PromotionFunction promotion function} object that randomly
	 * chooses ("promotes") two data objects.
	 *
	 * @param <DATA> The type of the data objects.
	 */
	public static class RandomPromotion<DATA> implements PromotionFunction<DATA> {
		@Override
		public Pair<DATA> process(Set<DATA> dataSet,
				DistanceFunction<? super DATA> distanceFunction) {
			List<DATA> promotedList = randomSample(dataSet, 2);
			return new Pair<DATA>(promotedList.get(0), promotedList.get(1));
		}
	}

	/**
	 * Randomly chooses elements from the collection.
	 * 
	 * @param collection The collection.
	 * @param n          The number of elements to choose.
	 * @param <T>        The type of the elements.
	 * @return A list with the chosen elements.
	 */
	public static <T> List<T> randomSample(Collection<T> collection, int n) {
		List<T> list = new ArrayList<T>(collection);
		List<T> sample = new ArrayList<T>(n);
		Random random = new Random();
		while (n > 0 && !list.isEmpty()) {
			int index = random.nextInt(list.size());
			sample.add(list.get(index));
			int indexLast = list.size() - 1;
			T last = list.remove(indexLast);
			if (index < indexLast) {
				list.set(index, last);
			}
			n--;
		}
		return sample;
	}

}
