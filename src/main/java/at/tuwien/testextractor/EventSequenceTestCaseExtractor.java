package at.tuwien.testextractor;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetyproject.lp.asp.semantics.AnswerSet;
import org.tweetyproject.lp.asp.syntax.ASPAtom;
import org.tweetyproject.lp.asp.syntax.ASPLiteral;

import java.util.*;

public class EventSequenceTestCaseExtractor implements TestCaseExtractor {
    private  final Logger logger = LoggerFactory.getLogger(EventSequenceTestCaseExtractor.class);

    public  List<List<StringProperty>> extract(AnswerSet answerSet, List<String> header) {
        logger.info("STARTING EXTRACTION...");

        if (answerSet == null || answerSet.getLiteralsWithName("hb").size() == 0) {
            throw new IllegalStateException("No Solution found");
        }

        int numEvents = header.size()-1;

        // order row atoms in answer set by row numbers
        Set<ASPLiteral> happensBeforeLiterals = answerSet.getLiteralsWithName("hb");
        List<ASPAtom> atomList = new ArrayList<>();
        for (ASPLiteral literal: happensBeforeLiterals) {
            atomList.add(literal.getAtom());
        }
        atomList = atomList.stream().sorted(new HappensBeforeAtomComparator()).toList();
        logger.debug("Sorted HappensBefore Atoms List: {}", atomList);

        List<List<StringProperty>> testCases = new ArrayList<>();

        // add header row for table column names
        List<StringProperty> headerRow = new ArrayList<>();
        for (String headerElement : header) {
            headerRow.add(new SimpleStringProperty(headerElement));
        }
        testCases.add(headerRow);

        int currentBeforeCounter = 0;
        StringProperty previousBeforeEvent = new SimpleStringProperty(getEventBefore(atomList.get(0)));
        String previousRow = "1";
        StringProperty[] currentEventOrder = new StringProperty[numEvents];
        HashSet<String> appearedBefore = new HashSet<>();
        HashSet<String> appearedAfter = new HashSet<>();
        for (int i = 0; i < atomList.size(); i++) {
            ASPAtom currentAtom = atomList.get(i);
            // check if current row is completed
            String currentRow = getRowNumber(currentAtom);
            logger.debug("CURRENT ROW: {}", currentRow);
            StringProperty currentBeforeEvent = new SimpleStringProperty(getEventBefore(currentAtom));
            logger.debug("CURRENT BEFORE EVENT: {}", currentBeforeEvent);
            StringProperty currentAfterEvent = new SimpleStringProperty(getEventAfter(currentAtom));
            logger.debug("CURRENT AFTER EVENT: {}", currentAfterEvent);

            if (!currentRow.equals(previousRow)) {
                // new row has started, finalize prior order and store it
                logger.debug("NEW ROW DETECTED");
                logger.debug("CURRENT BEFORE COUNTER: {}", currentBeforeCounter);
                int place = numEvents - (currentBeforeCounter + 1);
                currentEventOrder[place] = previousBeforeEvent;
                logger.debug("Current order status for row {}: {}", currentRow, Arrays.toString(currentEventOrder));
                currentEventOrder[numEvents-1] = findLastEvent(appearedBefore, appearedAfter);
                List<StringProperty> order = new ArrayList<>();
                order.add(new SimpleStringProperty(previousRow));
                order.addAll(Arrays.stream(currentEventOrder).toList());
                testCases.add(order);
                logger.debug("Extracted test case: {}", printCurrentTestCaseOrder(order));
                // reset everything
                currentBeforeCounter = 0;
                previousBeforeEvent = currentBeforeEvent;
                appearedBefore = new HashSet<>();
                appearedAfter = new HashSet<>();
                currentEventOrder = new StringProperty[numEvents];
            }

            // check if current beforeEvent is completed
            if (!currentBeforeEvent.getValue().equals(previousBeforeEvent.getValue())) {
                // new before event found, store previous event at calculated location and reset counter
                logger.debug("NEW BEFORE EVENT DETECTED!");
                logger.debug("CURRENT BEFORE COUNTER: {}", currentBeforeCounter);
                int place = numEvents - (currentBeforeCounter + 1);
                currentEventOrder[place] = previousBeforeEvent;
                currentBeforeCounter = 0;
                logger.debug("Current order status for row {}: {}", currentRow, Arrays.toString(currentEventOrder));
            }

            if (!appearedBefore.contains(currentBeforeEvent.getValue())) {
                appearedBefore.add(currentBeforeEvent.getValue());
            }

            appearedAfter.add(currentAfterEvent.getValue());
            currentBeforeCounter++;
            previousRow = currentRow;
            previousBeforeEvent = currentBeforeEvent;
        }
        // add last testcase
        logger.debug("NEW ROW DETECTED");
        logger.debug("CURRENT BEFORE COUNTER: {}", currentBeforeCounter);
        int place = numEvents - (currentBeforeCounter + 1);
        currentEventOrder[place] = previousBeforeEvent;
        logger.debug("Current order status for row {}: {}", previousRow, Arrays.toString(currentEventOrder));
        currentEventOrder[numEvents-1] = findLastEvent(appearedBefore, appearedAfter);
        List<StringProperty> order = new ArrayList<>();
        order.add(new SimpleStringProperty(previousRow));
        order.addAll(Arrays.stream(currentEventOrder).toList());
        testCases.add(order);
        logger.debug("Extracted test case: {}", printCurrentTestCaseOrder(order));

        return testCases;
    }

    // returns event that in current row only happened after others but never before others
    private StringProperty findLastEvent(HashSet<String> happenedBefore, HashSet<String> happenedAfter) {
        logger.debug("Current appearedBefore status: {}", Arrays.toString(happenedBefore.toArray()));
        logger.debug("Current appearedAfter status: {}", Arrays.toString(happenedAfter.toArray()));
        for (String event : happenedAfter) {
            if (!happenedBefore.contains(event)) {
                logger.debug("Found last event: {}", event);
                return new SimpleStringProperty(event);
            }
        }
        return null;
    }


    private String printCurrentTestCaseOrder(List<StringProperty> list) {
        String result = "";
        for (StringProperty event: list) {
            result = result.concat(event + "\t");
        }
        return result + "\n";
    }

    // utility methods for extracting information from row atoms
    private  String getRowNumber(ASPAtom hbAtom) {
        return hbAtom.getArguments().get(0).toString();
    }

    private  String getEventBefore(ASPAtom hbAtom) {
        return hbAtom.getArguments().get(1).toString();
    }

    private  String getEventAfter(ASPAtom hbAtom) {
        return hbAtom.getArguments().get(2).toString();
    }


    // establishes a comparator for hb atoms that is based on the first term that represents the row number
    // is used to sort the hb atoms of answer sets in ascending row number order to easier extract test cases
    private static class HappensBeforeAtomComparator implements Comparator<ASPAtom> {
        @Override
        public int compare(ASPAtom atom1, ASPAtom atom2) {
            //System.out.println("Atoms: " + atom1 + ", " + atom2);
            int row1 = Integer.parseInt(String.valueOf(atom1.getArguments().get(0)));
            int row2 = Integer.parseInt(String.valueOf(atom2.getArguments().get(0)));

            if (row1 == row2) {
                // compare by first event
                String firstEvent1 = String.valueOf(atom1.getArguments().get(1));
                String firstEvent2 = String.valueOf(atom2.getArguments().get(1));
                if (firstEvent1.equals(firstEvent2)) {
                    // compare by second event
                    String secondEvent1 = String.valueOf(atom1.getArguments().get(2));
                    String secondEvent2 = String.valueOf(atom2.getArguments().get(2));
                    return String.CASE_INSENSITIVE_ORDER.compare(secondEvent1, secondEvent2);
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(firstEvent1, firstEvent2);
                }
            } else {
                return Integer.compare(row1, row2);
            }
        }
    }
}
