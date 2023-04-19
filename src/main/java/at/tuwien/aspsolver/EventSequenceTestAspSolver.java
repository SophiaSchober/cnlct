package at.tuwien.aspsolver;

import at.tuwien.aspsolver.sequences.EventSequence;
import at.tuwien.aspsolver.sequences.FourWayEventSequence;
import at.tuwien.aspsolver.sequences.PairwiseEventSequence;
import at.tuwien.aspsolver.sequences.ThreeWayEventSequence;
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
import java.util.*;

public class EventSequenceTestAspSolver implements AspSolver{

    private  final Logger logger = LoggerFactory.getLogger(EventSequenceTestAspSolver.class);
    final String CLINGO_PATH = "src/main/resources";

    private final TestStrength testStrength;
    private String baseProgramPath;

    public EventSequenceTestAspSolver(TestStrength testStrength) {
        this.testStrength = testStrength;

        if (testStrength.equals(TestStrength.THREE_WAY)) {
            baseProgramPath = "src/main/resources/3-way-sca-greedy.lp";
        }
        else if (testStrength.equals(TestStrength.PAIRWISE)) {
            baseProgramPath = "src/main/resources/2-way-sca-greedy.lp";
        }
        else if (testStrength.equals(TestStrength.FOUR_WAY)) {
            baseProgramPath = "src/main/resources/4-way-sca-greedy.lp";
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
        int coveredBefore = -1;
        int coveredNow = 0;
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
            coveredAtomsList = coveredAtomsList.stream().sorted(new CoveredAtomComparator()).toList();
            HashSet<EventSequence> coveredAtoms = new HashSet<>();
            for (ASPAtom atom : coveredAtomsList) {
                EventSequence covered =  createNewParameterCombination(atom);
                coveredAtoms.add(covered);
            }
            coveredNow = coveredAtoms.size();
            logger.info("After {} iterations {} combinations are covered!", i, coveredNow);

            // extract rows and update intermediate result
            List<ASPLiteral> hbLiterals = currentAnswerSet.getLiteralsWithName("hb").stream().toList();
            logger.trace("Found rows: ");
            intermediateResult = "";
            for (ASPLiteral literal : hbLiterals) {
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

    private EventSequence createNewParameterCombination(ASPAtom atom) {
        if (testStrength.equals(TestStrength.PAIRWISE)) {
            return new PairwiseEventSequence(atom);
        }
        else if (testStrength.equals(TestStrength.THREE_WAY)) {
            return new ThreeWayEventSequence(atom);
        }
        else if (testStrength.equals(TestStrength.FOUR_WAY)) {
            return new FourWayEventSequence(atom);
        }
        return null;
    }


    // can be used for debugging purposes
    private static class CoveredAtomComparator implements Comparator<ASPAtom> {
        @Override
        public int compare(ASPAtom atom1, ASPAtom atom2) {
            String firstEvent1 = String.valueOf(atom1.getArguments().get(0));
            String firstEvent2 = String.valueOf(atom2.getArguments().get(0));

            if (firstEvent1.equals(firstEvent2)) {
                // compare by first event
                String secondEvent1 = String.valueOf(atom1.getArguments().get(1));
                String secondEvent2 = String.valueOf(atom2.getArguments().get(1));
                return String.CASE_INSENSITIVE_ORDER.compare(secondEvent1, secondEvent2);
            } else {
                return String.CASE_INSENSITIVE_ORDER.compare(firstEvent1, firstEvent2);
            }
        }
    }

}
