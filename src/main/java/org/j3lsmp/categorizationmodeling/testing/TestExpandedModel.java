package org.j3lsmp.categorizationmodeling.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.j3lsmp.categorizationmodeling.ExpandedModel;

class TestExpandedModel {

    final static Pattern DATA_LINE_REGEX = Pattern.compile(
			"^\\d+,\\d+,(\\d+),\\d+,\\d+,\\d+,([01]{5})(?:pt)?\\.jpg,\\d,(\\d),(\\d),\\d,\\d,[^,]+,\\d+,[^,]+,[^,]+,[^,]+,[^,]+$");

    public static void main(String[] args) throws IOException {
        System.out.println(testModel("home/jadenrunruh/categorization-modeling/AlienData.csv", true));
    }

    static double testModel(String path, boolean doPrints) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));

        int correctCount = 0, totalCount = 0;
        int lastSession = 1;
        for (int i = 0; i < lines.size() - 1; ) {
            ExpandedModel model = new ExpandedModel();
            while (i < lines.size() - 1) {
                i++;
                if (doPrints && i % 1000 == 0)
                    System.out.printf("Checking line %d of %d\n", i, lines.size() - 1);
                
                Matcher match = DATA_LINE_REGEX.matcher(lines.get(i));

                if (!match.find()) {
                    System.out.printf("Match not found on line %d\n", i);
                    continue;
                }

                int currentSession = Integer.parseInt(match.group(1));
                
                if (currentSession != lastSession) {
                    i--;
                    lastSession = currentSession;
                    break;
                }

                boolean[] traits = ExpandedModel.traitsFromStr(match.group(2), 5);
                boolean isDangerous = match.group(4).equals("1"), response = Integer.parseInt(match.group(3)) > 2;

                totalCount++;
                if ((model.getCertainty(traits) > 50d) == response)
                    correctCount++;

                model.updateWeights(traits, isDangerous);
            }
        }

        double ratio = (double) correctCount / totalCount;
        if (doPrints)
			System.out.printf("Of %d trials, %d were predicted correctly, for a %f ratio", totalCount, correctCount, ratio);
        return ratio;
    }
}