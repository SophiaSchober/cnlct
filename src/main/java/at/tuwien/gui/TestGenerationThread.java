package at.tuwien.gui;

import at.tuwien.aspsolver.AspSolver;
import at.tuwien.aspsolver.EventSequenceTestAspSolver;
import at.tuwien.aspsolver.InputParameterTestAspSolver;
import at.tuwien.cnltranslator.CnlTranslator;
import at.tuwien.cnltranslator.EventSequenceTestTranslator;
import at.tuwien.cnltranslator.InputParameterTestTranslator;
import at.tuwien.cnltranslator.UnknownSentencePatternException;
import at.tuwien.entity.TestStrength;
import at.tuwien.entity.TestType;
import at.tuwien.gui.controller.SpecificationTabController;
import at.tuwien.testextractor.EventSequenceTestCaseExtractor;
import at.tuwien.testextractor.InputParameterTestCaseExtractor;
import at.tuwien.testextractor.TestCaseExtractor;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetyproject.lp.asp.semantics.AnswerSet;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestGenerationThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TestGenerationThread.class);

    private final String TRANSLATION_ERROR_MESSAGE = "Please check that the right testing type has been selected " +
            "and all sentences match the supported sentence patterns for this testing type. \nFurthermore, make sure " +
            "that names start with a letter and no characters aside from letters of the English alphabet and digits are used.";

    private final SpecificationTabController specificationTabController;

    private final CnlTranslator translator;
    private final AspSolver solver;
    private final TestCaseExtractor extractor;

    private final String specification;


    public TestGenerationThread(SpecificationTabController specificationTabController, TestType testType,
                                TestStrength testStrength, String specification) {
        this.specificationTabController = specificationTabController;
        this.specification = specification;

        if (testType.equals(TestType.EVENT_SEQUENCE)) {
            translator = new EventSequenceTestTranslator();
            solver = new EventSequenceTestAspSolver(testStrength);
            extractor = new EventSequenceTestCaseExtractor();
        }
        else {
            translator = new InputParameterTestTranslator();
            solver = new InputParameterTestAspSolver(testStrength);
            extractor = new InputParameterTestCaseExtractor();
        }
    }


    @Override
    public void run() {
        List<ASPRule> aspTranslation;
        AnswerSet answerSet;
        try {
            aspTranslation = translator.translate(Arrays.stream(specification.split("\n")).toList());

            int minIterations = translator.getSeedTestCounter();
            List<String> headerRow = translator.getHeaderRow();

            try {
                answerSet = solver.solve(aspTranslation, minIterations);
                List<List<StringProperty>> testcases = extractor.extract(answerSet, headerRow);
                specificationTabController.updateTestCasesAsync(testcases);
                specificationTabController.clearErrorTextAsync();
            } catch (IOException e) {
                logger.error("An IOException occurred during solving", e);
                specificationTabController.updateErrorTextAsync("INTERNAL ERROR: An internal error occurred.");
                specificationTabController.appendErrorText("\nThis should not have happened, please contact us and report it.");
            }
        } catch (UnknownSentencePatternException e) {
            logger.error("TRANSLATION ERROR: ", e);
            specificationTabController.updateErrorTextAsync("TRANSLATION ERROR: " + e.getMessage());
                    specificationTabController.appendErrorText("\n" + TRANSLATION_ERROR_MESSAGE);
        } catch (Exception e) {
           logger.error("An unexpected error occurred: ", e);
            specificationTabController.updateErrorTextAsync("ERROR: An unexpected error occurred.");
            specificationTabController.appendErrorText("\n" + TRANSLATION_ERROR_MESSAGE);
        }
        specificationTabController.endTestCaseGenerationAsync();
    }

}
