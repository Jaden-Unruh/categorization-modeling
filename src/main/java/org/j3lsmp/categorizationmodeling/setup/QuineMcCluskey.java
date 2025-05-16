package org.j3lsmp.categorizationmodeling.setup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Quine-McCluskey algorithm implementation for minimizing boolean functions
 * given truth-tables/minterms
 * 
 * This program reads a csv file containing minterms in the first columns and
 * outputs the minimized boolean function to the next column.
 * 
 * @author Jaden Unruh
 */
public class QuineMcCluskey {

	/**
	 * The variable names used in the output.
	 */
	static String variableNames = "ABCDEFG";

	/**
	 * The number of variables in the boolean function.
	 */
	static final int NUM_VARS = 3;

	/**
	 * The main method to run the program.
	 * 
	 * @param args command line arguments (not used)
	 */
	public static void main(String[] args) {
		
		setupFile("E:\\Categorization modeling project\\3-values.csv", (int) Math.pow(2, NUM_VARS));

		runOnFile("E:\\Categorization modeling project\\3-values.csv");
		// runTest();
	}

	/**
	 * Sets up a CSV file with all possible combinations of binary values for the
	 * given number of variables.
	 * 
	 * @param path      the path to the file to create
	 * @param numValues the number of potential minterms (2^NUM_VARS)
	 */
	private static void setupFile(String path, int numValues) {
		System.out.printf("Setting up file with %f rows for truth tables of %f lines\n", Math.pow(2, Math.pow(2, NUM_VARS)), Math.pow(2, NUM_VARS));
		try {
			File file = new File(path);
			file.createNewFile();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < Math.pow(2, numValues); i++) {
				StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));
				while (binary.length() < numValues) {
					binary.insert(0, "0");
				}
				StringBuilder withCommas = new StringBuilder();
				for (int j = 0; j < binary.length(); j++) {
					withCommas.append(binary.charAt(j));
					withCommas.append(",");
				}
				writer.write(withCommas.toString());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads a file containing minterms and applies the Quine-McCluskey algorithm on
	 * each line, outputting to the next column
	 * 
	 * @param path the path to the file to read
	 */
	private static void runOnFile(String path) {
		try {
			List<String> lines = Files.readAllLines(Path.of(path));
			ArrayList<MinTerms> toDo = new ArrayList<>();
			for (String line : lines)
				toDo.add(new MinTerms(line));

			for (int i = 1; i < toDo.size(); i++) {
				MinTerms item = toDo.get(i);
				ArrayList<String> primeImplicants = getPrimeImplicants(item.minTerms);
				Map<String, String> primeImplicantChart = createPrimeImplicantChart(primeImplicants, item.minTerms);
				ArrayList<String> essentialPrimeImplicants = getEssentialPrimeImplicants(primeImplicantChart,
						item.minTerms);
				getAdditionalEssentialPrimeImplicants(essentialPrimeImplicants, primeImplicantChart, item.minTerms);

				StringBuilder output = new StringBuilder();
				
				output.append("\"" + essentialPrimeImplicants.toString() + "\",");
				
				boolean first = true;
				for (String essentialPrimeImplicant : essentialPrimeImplicants) {
					if (!first)
						output.append("v");
					first = false;
					output.append("(");
					StringBuilder conjunction = new StringBuilder();
					for (int j = 0; j < essentialPrimeImplicant.length(); j++) {
						if (essentialPrimeImplicant.charAt(j) == '1') {
							if (conjunction.length() != 0)
								conjunction.append("&");
							conjunction.append(variableNames.charAt(j));
						} else if (essentialPrimeImplicant.charAt(j) == '0') {
							if (conjunction.length() != 0)
								conjunction.append("&");
							conjunction.append("!" + variableNames.charAt(j));
						}
					}
					output.append(conjunction);
					output.append(")");
				}

				String originalLineContent = lines.get(i);
				lines.set(i, originalLineContent + output.toString());
			}
			Files.write(Path.of(path), lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A test method to run the algorithm with hardcoded minterms and print the
	 * results.
	 * 
	 * Based on the values used in the example on the Wikipedia Page
	 */
	@SuppressWarnings("unused")
	private static void runTest() {
		ArrayList<String> minTerms = new ArrayList<>();
		minTerms.add("0100");
		minTerms.add("1000");
		minTerms.add("1001");
		minTerms.add("1010");
		minTerms.add("1100");
		minTerms.add("1011");
		minTerms.add("1110");
		minTerms.add("1111");

		ArrayList<String> strictMinTerms = new ArrayList<>();
		strictMinTerms.add("0100");
		strictMinTerms.add("1000");
		strictMinTerms.add("1010");
		strictMinTerms.add("1100");
		strictMinTerms.add("1011");
		strictMinTerms.add("1111");

		ArrayList<String> primeImplicants = getPrimeImplicants(minTerms);

		System.out.println(primeImplicants);

		Map<String, String> primeImplicantChart = createPrimeImplicantChart(primeImplicants, strictMinTerms);

		System.out.println("\n\nPrime Implicant Chart:");
		for (Map.Entry<String, String> entry : primeImplicantChart.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}

		ArrayList<String> essentialPrimeImplicants = getEssentialPrimeImplicants(primeImplicantChart, strictMinTerms);
		System.out.println("\n\nFirst-order essential Prime Implicants:");
		System.out.println(essentialPrimeImplicants);

		getAdditionalEssentialPrimeImplicants(essentialPrimeImplicants, primeImplicantChart, strictMinTerms);
		System.out.println("\n\nAll essential Prime Implicants:");
		System.out.println(essentialPrimeImplicants);
	}

	/**
	 * Recursively finds all prime implicants from the given minterms.
	 * 
	 * @param minTerms the list of minterms to find prime implicants from
	 * @return a list of prime implicants
	 */
	private static ArrayList<String> getPrimeImplicants(ArrayList<String> minTerms) {
		System.out.println("minTerms: " + minTerms);
		ArrayList<String> primeImplicants = new ArrayList<>();
		ArrayList<Boolean> merges = new ArrayList<>();
		while (merges.size() < minTerms.size())
			merges.add(false);

		int numberOfMerges = 0;

		for (int i = 0; i < minTerms.size(); i++)
			for (int c = i + 1; c < minTerms.size(); c++) {
				String minTerm1 = minTerms.get(i), minTerm2 = minTerms.get(c);

				if (checkDashesAlign(minTerm1, minTerm2) && checkMinTermDifference(minTerm1, minTerm2)) {
					String mergedMinTerm = mergeMinTerms(minTerm1, minTerm2);
					if (!primeImplicants.contains(mergedMinTerm))
						primeImplicants.add(mergedMinTerm);
					merges.set(i, true);
					merges.set(c, true);
					numberOfMerges++;
				}
			}

		for (int i = 0; i < minTerms.size(); i++)
			if (!merges.get(i) && !primeImplicants.contains(minTerms.get(i)))
				primeImplicants.add(minTerms.get(i));

		if (numberOfMerges > 0)
			return getPrimeImplicants(primeImplicants);
		else
			return primeImplicants;
	}

	/**
	 * Creates a prime implicant chart from the given prime implicants and minterms.
	 * 
	 * @param primeImplicants the list of prime implicants
	 * @param minTerms        the list of minterms
	 * @return a map representing the prime implicant chart
	 */
	private static Map<String, String> createPrimeImplicantChart(ArrayList<String> primeImplicants,
			ArrayList<String> minTerms) {
		HashMap<String, String> primeImplicantChart = new HashMap<>();
		for (int i = 0; i < primeImplicants.size(); i++)
			primeImplicantChart.put(primeImplicants.get(i), "");

		for (String primeImplicant : primeImplicantChart.keySet()) {
			Pattern regex = convertToRegularExpression(primeImplicant);
			for (int j = 0; j < minTerms.size(); j++) {
				String minTerm = minTerms.get(j);
				if (regex.matcher(minTerm).matches())
					primeImplicantChart.put(primeImplicant, primeImplicantChart.get(primeImplicant) + "1");
				else
					primeImplicantChart.put(primeImplicant, primeImplicantChart.get(primeImplicant) + "0");
			}
		}

		return primeImplicantChart;
	}

	/**
	 * Finds the essential prime implicants from the prime implicant chart and
	 * minterms. Only finds the 'first-order' essential prime implicants, those with a corresponding minterm only reflected by one prime implicant.
	 * 
	 * @param primeImplicantChart the prime implicant chart
	 * @param minTerms            the list of minterms
	 * @return a list of essential prime implicants
	 * @see #getAdditionalEssentialPrimeImplicants
	 */
	private static ArrayList<String> getEssentialPrimeImplicants(Map<String, String> primeImplicantChart,
			ArrayList<String> minTerms) {
		ArrayList<String> essentialPrimeImplicants = new ArrayList<>();
		ArrayList<Entry<String, String>> minTermCoverages = new ArrayList<>(primeImplicantChart.entrySet());

		for (int i = 0; i < minTermCoverages.get(0).getValue().length(); i++) {
			int count = 0;
			String recentKey = "";
			for (int j = 0; j < minTermCoverages.size(); j++) {
				String coverage = minTermCoverages.get(j).getValue();
				if (coverage.charAt(i) == '1') {
					count++;
					recentKey = minTermCoverages.get(j).getKey();
				}
			}
			if (count == 1 && !essentialPrimeImplicants.contains(recentKey))
				essentialPrimeImplicants.add(recentKey);
		}

		return essentialPrimeImplicants;
	}

	/**
	 * Finds additional essential prime implicants from the prime implicant chart - those that are still required to cover minterms that were not covered by {@link #getEssentialPrimeImplicants(Map, ArrayList)}
	 * @param essentialPrimeImplicants the list of essential prime implicants
	 * @param primeImplicantChart the prime implicant chart
	 * @param minTerms the list of minterms
	 */
	private static void getAdditionalEssentialPrimeImplicants(ArrayList<String> essentialPrimeImplicants,
			Map<String, String> primeImplicantChart, ArrayList<String> minTerms) {
		for (String essentialPrimeImplicant : essentialPrimeImplicants) {
			String coverage = primeImplicantChart.get(essentialPrimeImplicant);
			for (int i = 0; i < coverage.length(); i++)
				if (coverage.charAt(i) == '1')
					minTerms.set(i, null);
		}

		for (int i = 0; i < minTerms.size(); i++) {
			if (minTerms.get(i) == null)
				continue;

			int count = -1;
			String recentKey = "";
			for (int j = 0; j < primeImplicantChart.size(); j++) {
				String primeImplicant = primeImplicantChart.keySet().toArray()[j].toString();
				String coverage = primeImplicantChart.get(primeImplicant);
				if (coverage.charAt(i) == '1') {
					int countHyphens = StringUtils.countMatches(primeImplicant, "-");
					if (countHyphens > count)
						recentKey = primeImplicant;
				}
			}
			if (!essentialPrimeImplicants.contains(recentKey))
				essentialPrimeImplicants.add(recentKey);
		}
	}

	/**
	 * Converts a prime implicant string to a regular expression pattern.
	 * 
	 * @param primeImplicant the prime implicant string
	 * @return the regular expression pattern
	 */
	private static Pattern convertToRegularExpression(String primeImplicant) {
		StringBuilder regex = new StringBuilder();
		for (int i = 0; i < primeImplicant.length(); i++)
			if (primeImplicant.charAt(i) == '-')
				regex.append("[01]");
			else
				regex.append(primeImplicant.charAt(i));
		return Pattern.compile(regex.toString());
	}
	
	/**
	 * Checks if the dashes in two minterms align.
	 * 
	 * @param minTerm1 the first minterm
	 * @param minTerm2 the second minterm
	 * @return true if the dashes align, false otherwise
	 */
	private static boolean checkDashesAlign(String minTerm1, String minTerm2) {
		for (int i = 0; i < minTerm1.length(); i++)
			if (minTerm1.charAt(i) == '-' && minTerm2.charAt(i) != '-')
				return false;
		return true;
	}

	/**
	 * Checks if the two minterms differ by only one bit.
	 * 
	 * @param minTerm1 the first minterm
	 * @param minTerm2 the second minterm
	 * @return true if they differ by one bit, false otherwise
	 */
	private static boolean checkMinTermDifference(String minTerm1, String minTerm2) {
		int intMinTerm1 = Integer.parseInt(minTerm1.replace("-", "0"), 2);
		int intMinTerm2 = Integer.parseInt(minTerm2.replace("-", "0"), 2);

		int res = intMinTerm1 ^ intMinTerm2;
		return res != 0 && (res & (res - 1)) == 0;
	}

	/**
	 * Merges two minterms into one by replacing the differing bit with a dash.
	 * 
	 * @param minTerm1 the first minterm
	 * @param minTerm2 the second minterm
	 * @return the merged minterm
	 */
	private static String mergeMinTerms(String minTerm1, String minTerm2) {
		StringBuilder mergedMinTerm = new StringBuilder();
		for (int i = 0; i < minTerm1.length(); i++)
			if (minTerm1.charAt(i) == minTerm2.charAt(i))
				mergedMinTerm.append(minTerm1.charAt(i));
			else
				mergedMinTerm.append('-');
		return mergedMinTerm.toString();
	}
}

/**
 * A class representing minterms in their binary representations.
 * 
 * @author Jaden Unruh
 */
class MinTerms {
	/**
	 * The list of minterms in binary representation.
	 */
	ArrayList<String> minTerms = new ArrayList<>();

	/**
	 * Constructor that takes a line from the file and parses it into minterms.
	 * 
	 * @param fileLine the line from the file
	 */
	MinTerms(String fileLine) {
		fileLine = fileLine.replaceAll(",", "");
		for (int i = 0; i < fileLine.length(); i++)
			if (fileLine.charAt(fileLine.length() - i - 1) == '1') {
				String binary = Integer.toBinaryString(i);
				while (binary.length() < QuineMcCluskey.NUM_VARS)
					binary = "0" + binary;
				minTerms.add(binary);
			}
	}
}