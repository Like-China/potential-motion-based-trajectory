package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import evaluation.Settings;

/**
 * Some utilities.
 */
public final class Utils {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private Utils() {
	}

	/**
	 * Identifies the minimum and maximum elements from an iterable, according
	 * to the natural ordering of the elements.
	 * 
	 * @param items An {@link Iterable} object with the elements
	 * @param <T>   The type of the elements.
	 * @return A pair with the minimum and maximum elements.
	 */
	public static <T extends Comparable<T>> Pair<T> minMax(Iterable<T> items) {
		Iterator<T> iterator = items.iterator();
		if (!iterator.hasNext()) {
			return null;
		}

		T min = iterator.next();
		T max = min;

		while (iterator.hasNext()) {
			T item = iterator.next();
			if (item.compareTo(min) < 0) {
				min = item;
			}
			if (item.compareTo(max) > 0) {
				max = item;
			}
		}

		return new Pair<T>(min, max);
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

	// Write results log
	public static void writeFile(String setInfo, String otherInfo) {
		try {
			File writeName = new File(Settings.data + "out.txt");
			writeName.createNewFile();
			try (FileWriter writer = new FileWriter(writeName, true);
					BufferedWriter out = new BufferedWriter(writer)) {
				out.write(setInfo);
				out.newLine();
				out.write(otherInfo);
				out.newLine();
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
