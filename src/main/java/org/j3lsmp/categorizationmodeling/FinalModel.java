package org.j3lsmp.categorizationmodeling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * The model as used by the web interface
 */
public class FinalModel {

	/**
	 * The number of variables to use. Cannot increase beyond 4.
	 */
	static final int NUM_VARS = 4;

	/**
	 * The confidence factor to use. Changing it doesn't do much.
	 */
	static final double CONFIDENCE_FACTOR = 0.1d;

	/**
	 * Initial weights. Effectively final after initialization.
	 */
	static double[] INITIAL_WEIGHTS = new double[(int) Math.pow(2, Math.pow(2, NUM_VARS))];

	/**
	 * Regex pattern to find prime implicants in the csv final generated in QuineMcCluskey.java
	 */
	static final Pattern PI_REGEX = Pattern.compile("\\[.*\\]");

	/**
	 * Values used for initial weighting based on how many prime implicants the hypothesis has, and how many dashes are in each PI
	 */
	static final double[] numImplicantVals = { 0, .2, .4, .5, .6, .7, .8, .9, 1 }, avgDashesVals = { 1, .8, .6, .4, .2 };

	/**
	 * The current weights of each hypothesis
	 */
	double[] weights;
	
	/**
	 * How many predictions have been made
	 */
	public int numAnswered = 0;
	/**
	 * How many correct predictions have been made
	 */
	public int numCorrect = 0;

	/**
	 * Static initializer. Sets up {@link #INITIAL_WEIGHTS}
	 */
	static {
		List<String> lines = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(FinalModel.class.getClassLoader().getResourceAsStream("setup/4-values.csv")))) {
			String line;
			while ((line = reader.readLine()) != null)
				lines.add(line);
		} catch (IOException e) {
			e.printStackTrace();
		}

		double[] simplicities = new double[(int) Math.pow(2, Math.pow(2, NUM_VARS))];

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i), primeImplicants;
			int numImplicants = 0;
			try {
				Matcher match = PI_REGEX.matcher(line);
				match.find();
				primeImplicants = match.group();
				numImplicants = StringUtils.countMatches(primeImplicants, ",") + 1;
			} catch (IllegalStateException e) {
				primeImplicants = "";
			}
			int avgDashes = 4;
			if (numImplicants != 0)
				avgDashes = Math.floorDiv(StringUtils.countMatches(primeImplicants, "-"), numImplicants);

			double nSimplicity = 1 - (numImplicantVals[numImplicants] * avgDashesVals[avgDashes]);
			simplicities[i] = nSimplicity;
		}

		INITIAL_WEIGHTS = normalize(simplicities);
	}

	/**
	 * Normalizes the given array to sum to 100, keeping relative ratios
	 * @param nums the array to normalize
	 * @return the normalized array
	 */
	private static double[] normalize(double[] nums) {
		double total = 0;
		for (double num : nums)
			total += num;

		double per = 100d / total;
		for (int i = 0; i < nums.length; i++)
			nums[i] *= per;

		return nums;
	}

	/**
	 * Sets up a model copying {@link #INITIAL_WEIGHTS}
	 */
	public FinalModel() {
		weights = INITIAL_WEIGHTS.clone();
	}

	/**
	 * Resets this model to {@link #INITIAL_WEIGHTS}
	 */
	public void resetModel() {
		weights = INITIAL_WEIGHTS.clone();
	}

	/**
	 * Updates the weights given a set of traits known to be edible/evil or not
	 * @param traits the traits
	 * @param isEvil the known characteristic of those traits
	 */
	public void updateWeights(boolean[] traits, boolean isEvil) {
		for (int i = 0; i < weights.length; i++)
			weights[i] *= isCorrect(i, traits, isEvil) ? 1 : CONFIDENCE_FACTOR;

		weights = normalize(weights);
	}

	/**
	 * Determines if the given hypothesis matches the given data
	 * @param row the index of the hypothesis to check
	 * @param traits the traits of a particular fruit
	 * @param isEvil whether that fruit is actually edible/evil
	 * @return true if this row is correct regarding that particular fruit
	 */
	private boolean isCorrect(int row, boolean[] traits, boolean isEvil) {
		return paddedBinary(row).charAt(indexFromTraits(traits)) == (isEvil ? '1' : '0');
	}

	/**
	 * Returns a padded (16 digit) binary representation of the integer
	 * @param i an integer to convert to binary
	 * @return a 16-digit binary string
	 */
	private static String paddedBinary(int i) {
		String s = Integer.toBinaryString(i);
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 16 - s.length())
			sb.append('0');
		sb.append(s);
		return sb.toString();
	}

	/**
	 * Returns the index of a hypothesis string that corresponds to this particular set of traits
	 * @param traits the set of traits
	 * @return the index of a hypothesis that will correspond
	 */
	private static int indexFromTraits(boolean[] traits) {
		try {
			return Integer.parseInt(strFromTraits(traits), 2);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Returns a binary string given a set of traits
	 * @param traits the traits
	 * @return a 4-digit binary string that represents those traits
	 */
	public static String strFromTraits(boolean[] traits) {
		StringBuilder sb = new StringBuilder();
		for (boolean b : traits)
			sb.append(b ? '1' : '0');
		return sb.toString();
	}

	/**
	 * Gets the certainty of this model that a particular fruit will be edible/evil
	 * @param traits the traits of the fruit
	 * @return a number between 0 and 100, where 100 is certainty of the fruit's edibility/evilness
	 */
	public double getCertainty(boolean[] traits) {
		double certainty = 0;
		int index = indexFromTraits(traits);
		for (int i = 0; i < weights.length; i++)
			if (paddedBinary(i).charAt(index) == '1')
				certainty += weights[i];
		return certainty;
	}
}