package at.tuwien.aspsolver;

import at.tuwien.aspsolver.combinations.FourWayParameterCombination;
import at.tuwien.aspsolver.combinations.PairwiseParameterCombination;
import at.tuwien.aspsolver.combinations.ParameterCombination;
import at.tuwien.aspsolver.combinations.ThreeWayParameterCombination;
import at.tuwien.entity.TestStrength;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetyproject.lp.asp.reasoner.ClingoSolver;
import org.tweetyproject.lp.asp.semantics.AnswerSet;
import org.tweetyproject.lp.asp.syntax.ASPAtom;
import org.tweetyproject.lp.asp.syntax.ASPLiteral;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class InputParameterTestAspSolver implements AspSolver {

    private  final Logger logger = LoggerFactory.getLogger(InputParameterTestAspSolver.class);

    final String CLINGO_PATH = "src/main/resources";

    private String baseProgramPath;
    private final TestStrength testStrength;



    public InputParameterTestAspSolver(TestStrength testStrength) {
        this.testStrength = testStrength;
        if (testStrength.equals(TestStrength.THREE_WAY)) {
            baseProgramPath = "src/main/resources/3-way-ca-greedy.lp";
        }
        else if (testStrength.equals(TestStrength.PAIRWISE)) {
            baseProgramPath = "src/main/resources/2-way-ca-greedy.lp";
        }
        else if (testStrength.equals(TestStrength.FOUR_WAY)) {
            baseProgramPath = "src/main/resources/4-way-ca-greedy.lp";
        }

    }

    @Override
    public AnswerSet solve(List<ASPRule> aspRules, int minIterations) throws IOException {
        logger.info("STARTING GREEDY SOLVER...");
        logger.trace("minimum iterations: {}", minIterations);
        // set up the clingo solver
        ClingoSolver clingoSolver = new ClingoSolver(CLINGO_PATH);

        // read user specification into a string
        String userSpecification = "";
        for (ASPRule rule : aspRules) {
            userSpecification = userSpecification.concat(rule.toString() + "\n");
        }
        // read the greedy base program into a string
        String baseString = Files.readString(Path.of(baseProgramPath));
        String intermediateResult = "";

        AnswerSet currentAnswerSet = null;
        AnswerSet lastAnswerSet = null;
        int coveredBefore = 0;
        int coveredNow = 1;
        int i = 1;
        while(coveredBefore != coveredNow || i <= minIterations + 1) {
            lastAnswerSet = currentAnswerSet;
            logger.debug("Solving for row {}", i);
            // set up the program for the current greedy stage and solve it
            coveredBefore = coveredNow;
            String program = userSpecification + "\n" + baseString + "\n" + intermediateResult;
            clingoSolver.setOptions("-c i=" + i);
            List<AnswerSet> answerSets = clingoSolver.getModels(program);
            currentAnswerSet = answerSets.get(0);  // most optimized answer set on index 0

            // extract covered atoms and check whether this step has covered anything new
            List<ASPAtom> coveredAtomsList = extractAtomList(currentAnswerSet.getLiteralsWithName("covered").stream().toList());
            //coveredAtomsList = coveredAtomsList.stream().sorted(new CoveredAtomComparator()).toList();
            HashSet<ParameterCombination> coveredAtoms = new HashSet<>();
            for (ASPAtom atom : coveredAtomsList) {
                ParameterCombination covered =  createNewParameterCombination(atom);
                coveredAtoms.add(covered);
            }
            coveredNow = coveredAtoms.size();
            logger.info("After {} iterations {} combinations are covered!", i, coveredNow);

            // extract rows and update intermediate result
            List<ASPLiteral> rowLiterals = currentAnswerSet.getLiteralsWithName("row").stream().toList();
           logger.trace("Found rows: ");
            intermediateResult = "";
            for (ASPLiteral literal : rowLiterals) {
                intermediateResult = intermediateResult.concat(literal.getAtom().toString() + ".\n");
               logger.trace(literal.getAtom().toString());
            }
            i++;
        }

        return lastAnswerSet;
    }

    private List<ASPAtom> extractAtomList(List<ASPLiteral> literals) {
        List<ASPAtom> atomList = new ArrayList<>();
        for (ASPLiteral literal : literals) {
            atomList.add(literal.getAtom());
        }
        return atomList;
    }

    private ParameterCombination createNewParameterCombination(ASPAtom atom) {
        if (testStrength.equals(TestStrength.PAIRWISE)) {
            return new PairwiseParameterCombination(atom);
        }
        else if (testStrength.equals(TestStrength.THREE_WAY)) {
            return new ThreeWayParameterCombination(atom);
        }
        else if (testStrength.equals(TestStrength.FOUR_WAY)) {
            return new FourWayParameterCombination(atom);
        }
        return null;
    }


    // can be used for debugging purposes
    private static class CoveredAtomComparator implements Comparator<ASPAtom> {
        @Override
        public int compare(ASPAtom atom1, ASPAtom atom2) {
            String firstParam1 = String.valueOf(atom1.getArguments().get(0));
            String firstParam2 = String.valueOf(atom2.getArguments().get(0));

            if (firstParam1.equals(firstParam2)) {
                // compare by first param
                String firstValue1 = String.valueOf(atom1.getArguments().get(1));
                String firstValue2 = String.valueOf(atom2.getArguments().get(1));
                if (firstValue1.equals(firstValue2)) {
                    // compare by second event
                    String secondParam1 = String.valueOf(atom1.getArguments().get(2));
                    String secondParam2 = String.valueOf(atom2.getArguments().get(2));
                    if (secondParam1.equals(secondParam2)) {
                        String secondValue1 = String.valueOf(atom1.getArguments().get(3));
                        String secondValue2 = String.valueOf(atom2.getArguments().get(3));
                        return String.CASE_INSENSITIVE_ORDER.compare(secondValue1, secondValue2);
                    }
                    else {
                        return String.CASE_INSENSITIVE_ORDER.compare(secondParam1, secondParam2);
                    }
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(firstValue1, firstValue2);
                }
            } else {
                return String.CASE_INSENSITIVE_ORDER.compare(firstParam1, firstParam2);
            }
        }
    }
}
