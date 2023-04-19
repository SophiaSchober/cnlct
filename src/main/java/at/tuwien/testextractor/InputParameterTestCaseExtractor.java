package at.tuwien.testextractor;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tweetyproject.lp.asp.semantics.AnswerSet;
import org.tweetyproject.lp.asp.syntax.*;

import java.util.*;

public class InputParameterTestCaseExtractor implements TestCaseExtractor {

    private  final Logger logger = LoggerFactory.getLogger(InputParameterTestCaseExtractor.class);

    public  List<List<StringProperty>> extract(AnswerSet answerSet, List<String> paramOrder) {
        logger.info("STARTING EXTRACTION...");
        int numParams = paramOrder.size();

        // create lookup structure for order of parameters (+1 to make space for row number in test cases)
        HashMap<String,Integer> paramIndices = new HashMap<>();
        for (int i = 0; i < paramOrder.size(); i++) {
            paramIndices.put(firstCharToLowerCase(paramOrder.get(i)), i);
        }
        logger.trace(paramIndices.toString());

        // order row atoms in answer set by row numbers
        Set<ASPLiteral> rowLiterals = answerSet.getLiteralsWithName("row");
        List<ASPAtom> atomList = new ArrayList<>();
        for (ASPLiteral literal: rowLiterals) {
            atomList.add(literal.getAtom());
        }
        atomList = atomList.stream().sorted(new RowAtomComparator()).toList();
        logger.debug("Sorted Row Atoms List: {}", atomList);

        List<List<StringProperty>> testCases = new ArrayList<>();

        // add header row for table column names
        List<StringProperty> headerRow = new ArrayList<>();
        for (String param : paramOrder) {
            headerRow.add(new SimpleStringProperty(param));
        }
        testCases.add(headerRow);

        // start parsing row atoms from answer set and accumulating test case data
        // initialize parameters
        String previousRow = "1";
        StringProperty[] currentTestCase = new StringProperty[numParams];
        currentTestCase[0] = new SimpleStringProperty("1");
        for (ASPAtom currentAtom : atomList) {
            // check whether parsing of previous row is complete
            String currentRow = getRowNumber(currentAtom);
            logger.trace(currentAtom.toString());
            if (!currentRow.equals(previousRow)) {
                // previous row complete, next row has started
                // store previous row in results and create new object for next row
                testCases.add(Arrays.stream(currentTestCase).toList());
                logger.debug("Extracted test case: {}", testCaseToString(Arrays.stream(currentTestCase).toList(), paramOrder));
                currentTestCase = new StringProperty[numParams];
                currentTestCase[0] = new SimpleStringProperty(currentRow);
            }
            // update row and get current values
            previousRow = currentRow;
            String currentParam = getParamName(currentAtom);
            logger.trace(currentParam);
            String currentValue = getParamValue(currentAtom);
            int paramIndex = paramIndices.get(currentParam);
            currentTestCase[paramIndex] = new SimpleStringProperty(currentValue);
        }
        if (atomList.size() > 0) {
            // store last row
            testCases.add(Arrays.stream(currentTestCase).toList());
            logger.debug("Extracted test case: {}", testCaseToString(Arrays.stream(currentTestCase).toList(), paramOrder));
        }

        return testCases;
    }

    // utility methods for extracting information from row atoms
    private  String getRowNumber(ASPAtom rowAtom) {
        return rowAtom.getArguments().get(0).toString();
    }

    private  String getParamName(ASPAtom rowAtom) {
        return rowAtom.getArguments().get(1).toString();
    }

    private  String getParamValue(ASPAtom rowAtom) {
        return rowAtom.getArguments().get(2).toString();
    }

    // utility method for debug logs of test case extraction results
    private  String testCaseToString(List<StringProperty> testcase, List<String> params) {
        String result = "";
        for (int i = 0; i < testcase.size(); i++) {
            result = result.concat(params.get(i) + ": " + testcase.get(i).getValue() + "\t");
        }
        return result;
    }

    // establishes a comparator for row atoms that is based on the first term that represents the row number
    // is used to sort the row atoms of answer sets in ascending row number order to easier extract test cases
    private static class RowAtomComparator implements Comparator<ASPAtom> {
        @Override
        public int compare(ASPAtom atom1, ASPAtom atom2) {
            int row1 = Integer.parseInt(String.valueOf(atom1.getArguments().get(0)));
            int row2 = Integer.parseInt(String.valueOf(atom2.getArguments().get(0)));
            return Integer.compare(row1,row2);
        }
    }

    private String firstCharToLowerCase(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

}
