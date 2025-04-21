package org.j3lsmp.categorizationmodeling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Model {
	static int numVars = 4;

	static final double CONFIDENCE_FACTOR = 0.1;

	static double[] weights = new double[(int) Math.pow(2, Math.pow(2, numVars))];

	/**
	 * Complexity of each hypothesis - 1 is most complex, i.e. 4 PIs with no dashes,
	 * 0 is least complex, i.e. 0 PIs
	 */
	static double[] complexity = new double[(int) Math.pow(2, Math.pow(2, numVars))];

	final static double[] numImplicantVals = { 0, .2, .4, .5, .6, .7, .8, .9, 1 };
	final static double[] avgDashesVals = { 1, .8, .6, .4, .2 };

	final static Pattern PI_REGEX = Pattern.compile("\\[.*\\]");
	final static Pattern DATA_LINE_REGEX = Pattern.compile(
			"^\\d+,\\d+,(\\d+),\\d+,\\d+,\\d+,([01]{5})(?:pt)?\\.jpg,\\d,(\\d),(\\d),\\d,\\d,[^,]+,\\d+,[^,]+,[^,]+,[^,]+,[^,]+$");

	static void initializeWeights(String valuesFileName) throws IOException {

		List<String> lines = Files.readAllLines(Paths.get(valuesFileName));

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

			double nComplexity = numImplicantVals[numImplicants] * avgDashesVals[avgDashes];
			complexity[i] = nComplexity;
		}

		double totalComplexity = 0;
		for (double complex : complexity)
			totalComplexity += (1 - complex);

		double weightPer = 100d / totalComplexity;

		for (int i = 0; i < complexity.length; i++) {
			weights[i] = (1 - complexity[i]) * weightPer;
		}

		for (int i = 0; i < lines.size(); i++) {
			StringBuilder sb = new StringBuilder(lines.get(i));
			sb.append("," + complexity[i] + "," + weights[i]);
			lines.set(i, sb.toString());
		}

		Files.write(Paths.get(valuesFileName), lines, StandardCharsets.UTF_8);
	}

	static double[] updateWeights(double[] currWeights, boolean[] traits, boolean isEvil) {
		for (int i = 0; i < currWeights.length; i++)
			currWeights[i] *= isCorrect(i, traits, isEvil) ? 1 : CONFIDENCE_FACTOR;

		currWeights = normalize(currWeights);

		return currWeights;
	}

	static double[] normalize(double[] nums) {
		double total = 0;
		for (double num : nums)
			total += num;

		double per = 100d / total;
		for (int i = 0; i < nums.length; i++)
			nums[i] *= per;

		return nums;
	}

	static boolean isCorrect(int row, boolean[] traits, boolean isEvil) {
		return paddedBinary(row).charAt(indexFromTraits(traits)) == (isEvil ? '1' : '0');
	}

	static double getCertainty(boolean[] traits) {
		double certainty = 0;
		int index = indexFromTraits(traits);

		for (int i = 0; i < weights.length; i++)
			if (paddedBinary(i).charAt(index) == '1')
				certainty += weights[i];

		return certainty;
	}

	static String paddedBinary(int i) {
		String s = Integer.toBinaryString(i);
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 16 - s.length())
			sb.append('0');
		sb.append(s);
		return sb.toString();
	}

	static int indexFromTraits(boolean[] traits) {
		StringBuilder sb = new StringBuilder();
		for (boolean b : traits)
			sb.append(b ? '1' : '0');
		return Integer.parseInt(sb.toString(), 2);
	}

	static boolean[] traitsFromString(String input) {
		boolean[] ret = { input.charAt(0) == '1', input.charAt(2) == '1', input.charAt(3) == '1',
				input.charAt(4) == '1' }; // Skip index 1 because legs are never a determining trait for danger in the
											// study
		return ret;
	}

	public static void main(String[] args) {
		try {
			/*initializeWeights("E:\\Categorization modeling project\\4-values.csv");

			boolean[] testTraits = { true, false, false, true };
			updateWeights(weights, testTraits, true);

			boolean[] testTraits2 = { true, false, true, true };

			for (int i = 0; i < weights.length; i++)
				System.out.printf("%s: %f\n", paddedBinary(i), weights[i]);

			System.out.println(getCertainty(testTraits));
			System.out.println(getCertainty(testTraits2));

			updateWeights(weights, testTraits2, false);

			boolean[] testTraits3 = { false, false, true, false };
			boolean[] testTraits4 = { false, false, false, false };

			System.out.println(getCertainty(testTraits3));
			System.out.println(getCertainty(testTraits4));*/
			
			testModel("E:\\Categorization modeling project\\TylenEtAl\\Data\\AlienData.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void testModel(String path) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(path));
		
		int correctCount = 0, totalCount = 0;
		int lastSession = 1;

		for (int i = 0; i < lines.size() - 1; ) {
			initializeWeights("E:\\Categorization modeling project\\4-values.csv");
			while (i < lines.size() - 1) {
				i++;
				if (i % 1000 == 0)
					System.out.println(i);
				Matcher match = DATA_LINE_REGEX.matcher(lines.get(i));
				if (!match.find()) {
					System.out.printf("Match not found on line %d\n", i);
					continue;
				}
				int currentSession = Integer.parseInt(match.group(1));
				boolean[] traits = traitsFromString(match.group(2));
				boolean isDangerous = match.group(4).equals("1"), response = Integer.parseInt(match.group(3)) > 2;
				
				if (currentSession != lastSession) {
					i--;
					lastSession = currentSession;
					break;
				}
				
				totalCount++;
				if ((getCertainty(traits) > 50d) == response)
					correctCount++;
				
				updateWeights(weights, traits, isDangerous);
			}
		}
		
		System.out.printf("Of %d trials, %d were predicted correctly, for a %f ratio", totalCount, correctCount, (double) correctCount / totalCount);
	}
}