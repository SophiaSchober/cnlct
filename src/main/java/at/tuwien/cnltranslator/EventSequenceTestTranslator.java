package at.tuwien.cnltranslator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetyproject.logics.commons.syntax.Constant;
import org.tweetyproject.logics.commons.syntax.Predicate;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.lp.asp.syntax.ASPAtom;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventSequenceTestTranslator implements CnlTranslator {

    private final Logger logger = LoggerFactory.getLogger(EventSequenceTestTranslator.class);

    // event-name followed by optional comma-separated list of event-names and an optional last event-name after " and ", ends with " are events", optional dot ending
    private final String eventDefinitionRegex = "(?<firstevent>\\w+)(?<events>\\s{0}(?:,\\s?\\w+)*)(?:,? and (?<lastevent>\\w+))? are events\\.?";

    // Case-insensitive start, list of at least two comma-separated events within parentheses specifying suborder, ends with "is tested", optional dot ending
    private final String seedTestRegex = "(?:Ensure|ensure) that \\((?<events>\\w+(,\\s?\\w+)+)\\) is tested\\.?";

    // Case-insensitive start, two event-names separated by " happens before ", optional dot ending.
    private final String exclusionRegex = "(?:Exclude|exclude) that (?<event1>\\w+) happens before (?<event2>\\w+)\\.?";

    // Optional case-insensitive start, two event-names separated by " happens before ", optional dot ending.
    private final String orderFixationRegex = "((?:Ensure|ensure) that )?(?<event1>\\w+) happens before (?<event2>\\w+)\\.?";


    private final Predicate eventPredicate = new Predicate("sym",1);
    private final Predicate happensBeforePredicate = new Predicate("hb",3);
    private final Variable variable = new Variable("X");

    private int seedTestCounter = 0;
    private int eventCounter = 0;
    private List<String> headerRow = new ArrayList<>();
    private HashSet<String> eventSet = new HashSet<>();

    public EventSequenceTestTranslator() {
    }
    public List<ASPRule> translate(List<String> cnlSpecification) throws UnknownSentencePatternException {
        logger.info("STARTING TRANSLATION...");
        headerRow.add("Nr.");

        List<ASPRule> translatedRules = new ArrayList<>();
        for (String line : cnlSpecification) {
            if (line == null || line.replace(" ", "").length() == 0) {
                continue; // skip empty lines
            }

            if (Pattern.matches(eventDefinitionRegex, line)) {
                logger.debug("\"{}\" matches eventDefinitionPattern", line);
                translatedRules.addAll(translateEventDefinition(line));
            }
            else if (Pattern.matches(seedTestRegex, line)) {
                logger.debug("\"{}\" matches seedTestPattern", line);
                translatedRules.addAll(translateSeedTest(line));
            }
            else if (Pattern.matches(exclusionRegex, line)) {
                logger.debug("\"{}\" matches exclusionPattern", line);
                translatedRules.add(translateExclusion(line));
            }
            else if (Pattern.matches(orderFixationRegex, line)) {
                logger.debug("\"{}\" matches orderFixationPattern", line);
                translatedRules.add(translateOrderFixation(line));
            } else {
                throw new UnknownSentencePatternException("Error while translating sentence \"" + line + "\": Unknown pattern");
            }
        }

        return translatedRules;
    }

    private List<ASPRule> translateEventDefinition(String eventDefinitionSentence) {
        logger.info("Translating event definition");
        List<ASPRule> rules = new ArrayList<>();
        Matcher matcher = Pattern.compile(eventDefinitionRegex).matcher(eventDefinitionSentence);
        List<String> events = new ArrayList<>();

        if (matcher.find()) {
            // extract first event
            String firstEvent = matcher.group("firstevent");
            logger.debug("Extracted first event name: {}", firstEvent);
            events.add(firstEvent);

            // extract additional events separated by commas
            String commaEventsGroup = matcher.group("events");
            if (commaEventsGroup != null && commaEventsGroup.length() > 0) {
                String[] commaEvents = commaEventsGroup.replace(" ", "").substring(1).split(",");
                events.addAll(Arrays.stream(commaEvents).toList());
                logger.debug("Extracted event names: {}", Arrays.toString(commaEvents));
            }

            // extract optional last event after "and" keyword
            String lastEvent = matcher.group("lastevent");
            if (lastEvent != null && lastEvent.length() > 0) {
                events.add(lastEvent);
                logger.debug("Extracted last event name: {}", lastEvent);
            }

            // map to ASP rules
            for (String event: events) {
                rules.add(createEventDefinitionRule(event));
                if (!eventSet.contains(event)) {
                    eventSet.add(event);
                    eventCounter++;
                    headerRow.add("Event#" + eventCounter);
                }
            }
        }
        return rules;
    }

    private List<ASPRule> translateSeedTest(String seedTestDefinition) {
        logger.debug("Translating seed test definition");
        seedTestCounter++;
        List<ASPRule> rules = new ArrayList<>();
        Matcher matcher = Pattern.compile(seedTestRegex).matcher(seedTestDefinition);

        if (matcher.find()) {
            // extract events
            String commaEventsGroup = matcher.group("events");
            String[] commaEvents = commaEventsGroup.replace(" ", "").split(",");
            List<String> events = new ArrayList<>(Arrays.stream(commaEvents).toList());
            logger.debug("Extracted events: {}", Arrays.toString(commaEvents));

            rules.addAll(createSeedTestRules(events));
        }
        return rules;
    }

    private ASPRule translateExclusion(String exclusionDefinition) {
        logger.debug("Translating exclusion definition");
        Matcher matcher = Pattern.compile(exclusionRegex).matcher(exclusionDefinition);

        if (matcher.find()) {
            // extract events
            String event1 = matcher.group("event1");
            String event2 = matcher.group("event2");
            logger.debug("Extracted events for excluding order: {}, {}", event1, event2);

            return createDoesNotHappenBeforeRule(event1, event2);
        }
        return null;
    }

    private ASPRule translateOrderFixation(String orderFixationDefinition) {
        logger.debug("Translating order fixation definition");
        Matcher matcher = Pattern.compile(orderFixationRegex).matcher(orderFixationDefinition);

        if (matcher.find()) {
            // extract events
            String event1 = matcher.group("event1");
            String event2 = matcher.group("event2");
            logger.debug("Extracted events for fixing order: {}, {}", event1, event2);

            return createDoesNotHappenBeforeRule(event2, event1);
        }
        return null;
    }

    private ASPRule createEventDefinitionRule (String event){
        ASPRule eventRule = new ASPRule();
        ASPAtom eventAtom = new ASPAtom();
        eventAtom.setPredicate(eventPredicate);
        eventAtom.addArgument(new Constant(firstCharToLowerCase(event)));
        eventRule.addToHead(eventAtom);
        logger.debug("Created ASP event definition rule: {}", eventRule);
        return eventRule;
    }

    private List<ASPRule> createSeedTestRules(List<String> events){
        String rowNumber = String.valueOf(seedTestCounter);
        List<ASPRule> seedTestRules = new ArrayList<>();

        for (int i = 0; i < events.size()-1; i++) {
            ASPRule seedRule = new ASPRule();
            ASPAtom seedAtom = new ASPAtom();

            seedAtom.setPredicate(happensBeforePredicate);
            seedAtom.addArgument(new Constant(rowNumber));
            String firstEvent = firstCharToLowerCase(events.get(i));
            String secondEvent = firstCharToLowerCase(events.get(i+1));
            seedAtom.addArgument(new Constant(firstEvent));
            seedAtom.addArgument(new Constant(secondEvent));

            seedRule.addToHead(seedAtom);
            logger.debug("Created ASP seed test rule: {}", seedRule);
            seedTestRules.add(seedRule);
        }

        return seedTestRules;
    }

    private ASPRule createDoesNotHappenBeforeRule(String event1, String event2) {
        ASPRule rule = new ASPRule();
        ASPAtom atom = new ASPAtom();
        atom.setPredicate(happensBeforePredicate);
        atom.addArgument(variable);
        atom.addArgument(new Constant(firstCharToLowerCase(event1)));
        atom.addArgument(new Constant(firstCharToLowerCase(event2)));
        rule.setBody(atom);
        logger.debug("Created ASP doesNotHappenBefore rule: {}", rule);
        return rule;
    }

    private String firstCharToLowerCase(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public List<String> getHeaderRow() {
        return headerRow;
    }

    @Override
    public int getSeedTestCounter() {
        return seedTestCounter;
    }

}
