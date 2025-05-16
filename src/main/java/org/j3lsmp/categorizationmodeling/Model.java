package org.j3lsmp.categorizationmodeling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Old model
 * @deprecated web-1.0.0
 */
public class Model {
	static int numVars = 4;

	static double CONFIDENCE_FACTOR = 0.1;
	
	static double[] weights = new double[(int) Math.pow(2, Math.pow(2, numVars))];
	
	static double[] INITIAL_WEIGHTS = new double[(int) Math.pow(2, Math.pow(2, numVars))];

	/**
	 * Complexity of each hypothesis - 1 is most complex, i.e. 4 PIs with no dashes,
	 * 0 is least complex, i.e. 0 PIs
	 */
	static double[] complexity = new double[(int) Math.pow(2, Math.pow(2, numVars))];

	static double[] numImplicantVals = { 0, .2, .4, .5, .6, .7, .8, .9, 1 };
	static double[] avgDashesVals = { 1, .8, .6, .4, .2 };

	final static Pattern PI_REGEX = Pattern.compile("\\[.*\\]");
	final static Pattern DATA_LINE_REGEX = Pattern.compile(
			"^\\d+,\\d+,(\\d+),\\d+,\\d+,\\d+,([01]{5})(?:pt)?\\.jpg,\\d,(\\d),(\\d),\\d,\\d,[^,]+,\\d+,[^,]+,[^,]+,[^,]+,[^,]+$");

	static void initializeWeights(double[] currWeights, String valuesFileName, boolean write) throws IOException {

		List<String> lines = Files.readAllLines(Path.of(valuesFileName));

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
			currWeights[i] = (1 - complexity[i]) * weightPer;
		}
		
		System.out.println(Arrays.toString(currWeights));
		
		if (write) {
			for (int i = 0; i < lines.size(); i++) {
				StringBuilder sb = new StringBuilder(lines.get(i));
				sb.append("," + complexity[i] + "," + weights[i]);
				lines.set(i, sb.toString());
			}
	
			Files.write(Path.of(valuesFileName), lines, StandardCharsets.UTF_8);
		}
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

	static double getCertainty(double[] currWeights, boolean[] traits) {
		double certainty = 0;
		int index = indexFromTraits(traits);

		for (int i = 0; i < currWeights.length; i++)
			if (paddedBinary(i).charAt(index) == '1')
				certainty += currWeights[i];

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

	/**
	 * Entry method, tests the model. Unused as of web release.
	 * @param args unused
	 */
	public static void main(String[] args) {
		try {
			testModel("E:\\Categorization modeling project\\TylenEtAl\\Data\\AlienData.csv", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static double testModelValues(String path, double updateVal, double[] numImplicantVals, double[] numDashesVals, ArrayList<Session> sessions) throws IOException {
		double[] currWeights = new double[(int) Math.pow(2, Math.pow(2, numVars))];
		
		if (!Arrays.equals(Model.numImplicantVals, numImplicantVals) || !Arrays.equals(Model.avgDashesVals, numDashesVals) || INITIAL_WEIGHTS[21] == 0) {
			System.out.println("Values not equal - updating...");
			Model.numImplicantVals = numImplicantVals;
			Model.avgDashesVals = numDashesVals;
			initializeWeights(currWeights, "E:\\Categorization modeling project\\4-values.csv", false);
			INITIAL_WEIGHTS = currWeights.clone();
		}
		
		Model.CONFIDENCE_FACTOR = updateVal;
		
		int correct = 0, total = 0;
		
		for (int i = 0; i < sessions.size(); i++) {
			Session session = sessions.get(i);
			currWeights = INITIAL_WEIGHTS.clone();
			for (int j = 0; j < session.trials().size(); j++) {
				Trial trial = session.trials().get(j);
				if ((getCertainty(currWeights, trial.traits()) > 50d) == trial.response())
					correct++;
				total++;
				
				currWeights = updateWeights(currWeights, trial.traits(), trial.isDangerous());
			}
		}
		
		return (double) correct / total;
	}

	static double testModel(String path, boolean doPrints) throws IOException {
		List<String> lines = Files.readAllLines(Path.of(path));
		
		int correctCount = 0, totalCount = 0;
		int lastSession = 1;

		for (int i = 0; i < lines.size() - 1; ) {
			initializeWeights(weights, "E:\\Categorization modeling project\\4-values.csv", false);
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
				if ((getCertainty(weights, traits) > 50d) == response)
					correctCount++;
				
				if (i < 104)
					System.out.printf("Session 1, Trial %d: certainty = %f, response = %b\n", i, getCertainty(weights, traits), response);
				
				updateWeights(weights, traits, isDangerous);
			}
		}
		if (doPrints)
			System.out.printf("Of %d trials, %d were predicted correctly, for a %f ratio", totalCount, correctCount, (double) correctCount / totalCount);
		return (double) correctCount / totalCount;
	}
}