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

public class ExpandedQMcC {

	static String variableNames = "ABCDEFGHIJKLM";

	static final int NUM_VARS = 7, CONSIDERED_VARS = 4;

	final static List<List<Boolean>> COMBINATIONS = combinations(NUM_VARS, CONSIDERED_VARS);

	public static void main(String[] args) {
		setupFile(String.format("E:\\Categorization modeling project\\%dc%d-values.csv", NUM_VARS, CONSIDERED_VARS));
		runOnFile(String.format("E:\\Categorization modeling project\\%dc%d-values.csv", NUM_VARS, CONSIDERED_VARS));
	}

	private static void setupFile(String path) {
		try {
			File file = new File(path);
			file.createNewFile();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (List<Boolean> combination : COMBINATIONS) {
				for (int i = 0; i < Math.pow(2, Math.pow(2, CONSIDERED_VARS)); i++) {
					StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));
					while (binary.length() < Math.pow(2, CONSIDERED_VARS))
						binary.insert(0, "0");
					StringBuilder withCommas = new StringBuilder();
					for (int j = 0; j < binary.length(); j++) {
						withCommas.append(binary.charAt(j));
						withCommas.append(",");
					}
					writer.write(withCommas.toString() + getComboPortion(combination));
					writer.newLine();
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runOnFile(String path) {
		try {
			List<String> lines = Files.readAllLines(Path.of(path));
			ArrayList<MinTerms> toDo = new ArrayList<>();
			for (String line : lines)
				toDo.add(new MinTerms(line, (int) Math.pow(2, CONSIDERED_VARS)));

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

	private static ArrayList<String> getPrimeImplicants(ArrayList<String> minTerms) {
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

	private static ArrayList<String> getEssentialPrimeImplicants(Map<String, String> primeImplicantChart,
			ArrayList<String> minTerms) {
		ArrayList<String> essentialPrimeImplicants = new ArrayList<>();
		ArrayList<Entry<String, String>> minTermCoverages = new ArrayList<>(primeImplicantChart.entrySet());

		if (minTermCoverages.size() == 0)
			return essentialPrimeImplicants;

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

	private static Pattern convertToRegularExpression(String primeImplicant) {
		StringBuilder regex = new StringBuilder();
		for (int i = 0; i < primeImplicant.length(); i++)
			if (primeImplicant.charAt(i) == '-')
				regex.append("[01]");
			else
				regex.append(primeImplicant.charAt(i));
		return Pattern.compile(regex.toString());
	}

	private static boolean checkDashesAlign(String minTerm1, String minTerm2) {
		for (int i = 0; i < minTerm1.length(); i++)
			if (minTerm1.charAt(i) == '-' && minTerm2.charAt(i) != '-')
				return false;
		return true;
	}

	private static boolean checkMinTermDifference(String minTerm1, String minTerm2) {
		int intMinTerm1 = Integer.parseInt(minTerm1.replace("-", "0"), 2);
		int intMinTerm2 = Integer.parseInt(minTerm2.replace("-", "0"), 2);

		int res = intMinTerm1 ^ intMinTerm2;
		return res != 0 && (res & (res - 1)) == 0;
	}

	private static String mergeMinTerms(String minTerm1, String minTerm2) {
		StringBuilder mergedMinTerm = new StringBuilder();
		for (int i = 0; i < minTerm1.length(); i++)
			if (minTerm1.charAt(i) == minTerm2.charAt(i))
				mergedMinTerm.append(minTerm1.charAt(i));
			else
				mergedMinTerm.append('-');
		return mergedMinTerm.toString();
	}

	private static List<List<Boolean>> combinations(int n, int k) {
		List<List<Boolean>> result = new ArrayList<>();
		backtrack(n, k, 0, new ArrayList<>(), result);
		return result;
	}

	private static String getComboPortion(List<Boolean> combination) {
		StringBuilder binary = new StringBuilder();
		for (int i = 0; i < combination.size(); i++) {
			if (combination.get(i))
				binary.append("1");
			else
				binary.append("0");
			binary.append(",");
		}
		return binary.toString();
	}

	private static void backtrack(int n, int k, int start, List<Boolean> current, List<List<Boolean>> result) {
		if (current.size() == n) {
			if (k == 0)
				result.add(new ArrayList<>(current));
			return;
		}

		if (k > 0) {
			current.add(true);
			backtrack(n, k - 1, start + 1, current, result);
			current.remove(current.size() - 1);
		}

		if (n - current.size() > k) {
			current.add(false);
			backtrack(n, k, start + 1, current, result);
			current.remove(current.size() - 1);
		}
	}
}
