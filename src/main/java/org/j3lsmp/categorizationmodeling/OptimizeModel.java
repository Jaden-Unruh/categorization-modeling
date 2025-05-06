package org.j3lsmp.categorizationmodeling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class OptimizeModel {
	
	static final String PATH = "E:\\Categorization modeling project\\TylenEtAl\\Data\\AlienData.csv";
	
	static final double TOLERANCE = 1e-5;
	
	static double currConfFactor = 0.1;
	
	static double[] currNumImplicantVals = { 0, .2, .4, .5, .6, .7, .8, .9, 1 };
	static double[] currNumDashesVals = { 1, .8, .6, .4, .2 };
	
	static ArrayList<Session> sessions = new ArrayList<>();
	
	/**
	 * Ternary search to optimize the confidence factor. Assume results of model are continuous (probably true) and have only one peak (probably not true)
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		parseTrials(PATH);
		System.out.printf("Parsed %d sessions\n", sessions.size());
		System.out.println(Model.testModelValues(PATH, .1, currNumImplicantVals, currNumDashesVals, sessions));
		runOptimizations();
	}
	
	/**
	 * Run optimizations
	 */
	static void runOptimizations() throws IOException {
		double left = 0d, right = 1d;
		
		System.out.println("Optimizing...");
		
		while (right - left > TOLERANCE) {
			double mid1 = left + (right - left) / 3d, mid2 = right - (right - left) / 3d;
			
			System.out.printf("\nTesting values %f and %f\n", mid1, mid2);
			double f1 = Model .testModelValues(PATH, mid1, currNumImplicantVals, currNumDashesVals, sessions),
					f2 = Model.testModelValues(PATH, mid2, currNumImplicantVals, currNumDashesVals, sessions);
			
			System.out.println(Math.max(f1, f2));
			
			if (f1 < f2)
				left = mid1;
			else
				right = mid2;
		}
		
		System.out.printf("Optimized confidence factor is %f\n", (left + right) / 2d);
		System.out.println((left + right) / 2d);
	}
	
	/**
	 * Parse trials file
	 * @throws IOException 
	 */
	static void parseTrials(String path) throws IOException {
		List<String> lines = Files.readAllLines(Path.of(path));
		
		int lastSession = 1;
		int sessionIndex = 0;
		
		sessions.add(new Session(new LinkedList<Trial>()));
		
		for (int i = 0; i < lines.size() - 1; ) {
			while (i < lines.size() - 1) {
				i++;
				Matcher match = Model.DATA_LINE_REGEX.matcher(lines.get(i));
				if (!match.find()) {
					System.out.printf("Match not found on line %d\n", i);
					continue;
				}
				int currentSession = Integer.parseInt(match.group(1));
				boolean[] traits = Model.traitsFromString(match.group(2));
				boolean isDangerous = match.group(4).equals("1"), response = Integer.parseInt(match.group(3)) > 2;
				
				if (currentSession != lastSession) {
					i--;
					lastSession = currentSession;
					sessionIndex++;
					sessions.add(new Session(new LinkedList<Trial>()));
					break;
				}
				
				sessions.get(sessionIndex).trials().offer(new Trial(response, traits, isDangerous));
			}
		}
	}
}

record Trial(boolean response, boolean[] traits, boolean isDangerous) {}

record Session(LinkedList<Trial> trials) {}