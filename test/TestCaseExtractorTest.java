import at.tuwien.testextractor.EventSequenceTestCaseExtractor;
import at.tuwien.testextractor.InputParameterTestCaseExtractor;
import at.tuwien.testextractor.TestCaseExtractor;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;
import org.tweetyproject.lp.asp.reasoner.ClingoSolver;
import org.tweetyproject.lp.asp.semantics.AnswerSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCaseExtractorTest {

    // only simple sanity checks, content can be manually checked by turning on debug logs and comparing the printed
    // sorted row atoms list of the answer set with the printed test case extraction results
    @Test
    public void testInputParameterTestingTestCaseExtraction() throws IOException {
        // set up solver
        String clingo_path = "src/main/resources";
        ClingoSolver clingoSolver = new ClingoSolver(clingo_path);
        clingoSolver.setOptions("-c n=4");
        // create simple dummy program
        String databaseString = Files.readString(Path.of("test/resources/database-ca.lp"));
        String baseString = Files.readString(Path.of("test/resources/2-way-ca-gen.lp"));
        String program = databaseString + baseString;

        // collect answer set
        List<AnswerSet> as = clingoSolver.getModels(program);
        AnswerSet result = as.get(0);

        // extract test cases
        TestCaseExtractor extractor = new InputParameterTestCaseExtractor();
        List<String> headerRow = Arrays.asList("Nr.", "yearsEmployed", "carsSold", "complaints");
        List<List<StringProperty>> testcases = extractor.extract(result, headerRow);

        assertAll(
                () -> assertEquals(5, testcases.size()),
                () -> assertEquals(4, testcases.get(0).size()),
                () -> assertEquals(4, testcases.get(1).size()),
                () -> assertEquals(4, testcases.get(2).size()),
                () -> assertEquals(4, testcases.get(3).size()),
                () -> assertEquals(4, testcases.get(4).size())
        );
    }

    @Test
    public void testEventSequenceTestingTestCaseExtraction() throws IOException {
        // set up solver
        String clingo_path = "src/main/resources";
        ClingoSolver clingoSolver = new ClingoSolver(clingo_path);
        clingoSolver.setOptions("-c n=6");
        // create simple dummy program
        String databaseString = Files.readString(Path.of("test/resources/database-sca.lp"));
        String baseString = Files.readString(Path.of("test/resources/3-way-sca-gen.lp"));
        String program = databaseString + baseString;

        // collect answer set
        List<AnswerSet> as = clingoSolver.getModels(program);
        AnswerSet result = as.get(0);
        //System.out.println(result);

        // extract test cases
        TestCaseExtractor extractor = new EventSequenceTestCaseExtractor();
        List<String> headerRow = Arrays.asList("Nr.", "Event#1", "Event#2", "Event#3", "Event#4");
        List<List<StringProperty>> testcases = extractor.extract(result, headerRow);

        assertAll(
                () -> assertEquals(7, testcases.size()),
                () -> assertEquals(5, testcases.get(0).size()),
                () -> assertEquals(5, testcases.get(1).size()),
                () -> assertEquals(5, testcases.get(2).size()),
                () -> assertEquals(5, testcases.get(3).size()),
                () -> assertEquals(5, testcases.get(4).size()),
                () -> {
                    for (List<StringProperty> testCase : testcases) {
                        List<String> events = new ArrayList<>();
                        testCase.forEach(s -> events.add(s.getValue()));
                        assertAll(
                                () -> assertTrue(events.contains("Nr.") || events.contains("scan")),
                                () -> assertTrue(events.contains("Nr.") || events.contains("p1")),
                                () -> assertTrue(events.contains("Nr.") || events.contains("p2")),
                                () -> assertTrue(events.contains("Nr.") || events.contains("p3"))
                        );
                    }
                }
        );
    }
}
