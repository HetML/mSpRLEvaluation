package edu.tulane.cs.hetml;

import edu.tulane.cs.hetml.mSpRL.Eval.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class Main {

    private static String actualFile;
    private static String predictedFile;
    private static String specificTypeEval;
    private static File outputFile;
    private static String matchingType;
    private static EvalComparer comparer;
    private static FileOutputStream outputStream;

    public static void main(String[] args) throws IOException {
        readArgs(args);
        XmlSpRLEvaluator xmlEvaluator = new XmlSpRLEvaluator(actualFile, predictedFile, comparer);
        SpRLEvaluator evaluator = new SpRLEvaluator();

        List<SpRLEvaluation> roleResults = xmlEvaluator.evaluateRoles();
        printResults("Role results", roleResults);

        List<SpRLEvaluation> relationResults = xmlEvaluator.evaluateRelations();
        printResults("Relation results", relationResults);

        SpRLEvaluation relEval = relationResults.get(0);

        List<SpRLEvaluation> generalTypeResults = evaluator.evaluateRelationGeneralType(relEval);
        printResults("General Type results", generalTypeResults);

        List<SpRLEvaluation> rcc8Results = evaluator.evaluateRelationRCC8(relEval);
        switch (specificTypeEval) {
            case "a":
                printResults("Specific Value results", rcc8Results);
                break;

            case "ex-d":
                removeDistanceSpecificValues(relEval, rcc8Results);
                printResults("Specific Value results(distance relations excluded)", rcc8Results);
                break;

            case "ex-dm":
                removeDistanceSpecificValues(relEval, rcc8Results);
                removeMultiLabeledSpecificValues(relEval, rcc8Results);
                printResults("Specific Value results(distance and multi-label relations excluded)", rcc8Results);
                break;
        }
        outputStream.close();
    }

    private static void removeDistanceSpecificValues(SpRLEvaluation relEval, List<SpRLEvaluation> rcc8Results) {
        HashSet<String> distanceLabels = getSpecificValues(relEval, "DISTANCE");
        removeEvaluations(rcc8Results, distanceLabels);
    }

    private static void removeMultiLabeledSpecificValues(SpRLEvaluation relEval, List<SpRLEvaluation> rcc8Results) {
        HashSet<String> multiLabels = getSpecificValues(relEval, "/");
        removeEvaluations(rcc8Results, multiLabels);
    }

    private static void removeEvaluations(List<SpRLEvaluation> rcc8Results, HashSet<String> labelsToRemove) {
        for (int i = rcc8Results.size() - 1; i >= 0; i--) {
            SpRLEvaluation e = rcc8Results.get(i);
            if (labelsToRemove.contains(e.getLabel().toUpperCase())) {
                rcc8Results.remove(e);
            }
        }
    }

    private static HashSet<String> getSpecificValues(SpRLEvaluation relEval, String generalTypeContains) {
        HashSet<String> labels = new HashSet<>();
        relEval.getFn().forEach(r -> {
            RelationEval e = (RelationEval) r;
            if (e.getGeneralType().contains(generalTypeContains)) {
                labels.add(e.getRCC8().toUpperCase());
            }
        });
        relEval.getTp().forEach((a, p) -> {
            RelationEval e = (RelationEval) a;
            if (e.getGeneralType().toUpperCase().contains(generalTypeContains)) {
                labels.add(e.getRCC8().toUpperCase());
            }
        });
        return labels;
    }

    private static void printResults(String caption, List<SpRLEvaluation> evals) {
        SpRLEvaluator.printEvaluation(caption, evals);
        SpRLEvaluator.printEvaluation(caption, outputStream, evals);
    }

    private static void readArgs(String[] args) {
        if (args.length != 5) {
            error("Usage: mSpRLEval actual.xml predicted.xml output.txt matching(o:overlap/e:exact) " +
                    "specifiTypeEvaluation(a:all/ex-d:exclude distance/ex-dm: exclude distance amd multi label)");
        }

        actualFile = args[0];
        if (!new File(actualFile).exists())
            error("actual file doesn't exist.");

        predictedFile = args[1];
        if (!new File(predictedFile).exists())
            error("predictions file doesn't exist.");

        outputFile = new File(args[2]);
        outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        matchingType = args[3].toLowerCase();
        if (!matchingType.equals("o") && !matchingType.equals("e"))
            error("invalid matching type.");
        if (matchingType.equals("o"))
            comparer = new OverlapComparer();
        else
            comparer = new ExactComparer();

        specificTypeEval = args[4].toLowerCase();
        if (!specificTypeEval.equals("a") && !specificTypeEval.equals("ex-d") && !specificTypeEval.equals("ex-dm"))
            error("invalid specifiTypeEvaluation.");

    }

    private static void error(String message) {
        System.out.println(message);
        System.exit(1);
    }
}
