package org.j3lsmp.categorizationmodeling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class FinalModel {

	static final int NUM_VARS = 4;

	static final double CONFIDENCE_FACTOR = 0.1d;

	static double[] INITIAL_WEIGHTS = new double[(int) Math.pow(2, Math.pow(2, NUM_VARS))];

	static final Pattern PI_REGEX = Pattern.compile("\\[.*\\]");
	static final Pattern DATA_LINE_REGEX = Pattern.compile(
			"^\\d+,\\d+,(\\d+),\\d+,\\d+,\\d+,([01]{5})(?:pt)?\\.jpg,\\d,(\\d),(\\d),\\d,\\d,[^,]+,\\d+,[^,]+,[^,]+,[^,]+,[^,]+$");

	static final double[] numImplicantVals = { 0, .2, .4, .5, .6, .7, .8, .9, 1 };
	static final double[] avgDashesVals = { 1, .8, .6, .4, .2 };

	double[] weights;
	
	public int numAnswered = 0;
	public int numCorrect = 0;

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

	private static double[] normalize(double[] nums) {
		double total = 0;
		for (double num : nums)
			total += num;

		double per = 100d / total;
		for (int i = 0; i < nums.length; i++)
			nums[i] *= per;

		return nums;
	}

	public FinalModel() {
		weights = INITIAL_WEIGHTS.clone();
	}

	public void resetModel() {
		weights = INITIAL_WEIGHTS.clone();
	}

	public void updateWeights(boolean[] traits, boolean isEvil) {
		for (int i = 0; i < weights.length; i++)
			weights[i] *= isCorrect(i, traits, isEvil) ? 1 : CONFIDENCE_FACTOR;

		weights = normalize(weights);
	}

	private boolean isCorrect(int row, boolean[] traits, boolean isEvil) {
		return paddedBinary(row).charAt(indexFromTraits(traits)) == (isEvil ? '1' : '0');
	}

	private static String paddedBinary(int i) {
		String s = Integer.toBinaryString(i);
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 16 - s.length())
			sb.append('0');
		sb.append(s);
		return sb.toString();
	}

	private static int indexFromTraits(boolean[] traits) {
		try {
			return Integer.parseInt(strFromTraits(traits), 2);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public static String strFromTraits(boolean[] traits) {
		StringBuilder sb = new StringBuilder();
		for (boolean b : traits)
			sb.append(b ? '1' : '0');
		return sb.toString();
	}

	public double getCertainty(boolean[] traits) {
		double certainty = 0;
		int index = indexFromTraits(traits);
		for (int i = 0; i < weights.length; i++)
			if (paddedBinary(i).charAt(index) == '1')
				certainty += weights[i];
		return certainty;
	}
}