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
